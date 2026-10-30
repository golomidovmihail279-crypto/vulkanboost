package dev.vulkanboost.memory;

import dev.vulkanboost.autotune.MinecraftOptionAccess;
import dev.vulkanboost.config.VulkanBoostConfig;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.OptionalInt;

/**
 * Opt-in memory-pressure assistance.
 *
 * It does not call System.gc() or attempt to free memory directly. After sustained
 * heap pressure it lowers render/simulation distance to reduce the active chunk
 * workload, then restores the previous values after pressure subsides if the user
 * has not changed those options manually.
 */
public final class SmartMemoryManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanBoost/Memory");
    private static final int SAMPLE_INTERVAL_TICKS = 100;
    private static final int REQUIRED_HIGH_SAMPLES = 3;
    private static final int REQUIRED_LOW_SAMPLES = 6;
    private static final long ADJUSTMENT_COOLDOWN_MS = 60_000L;

    private static int ticks;
    private static int highPressureSamples;
    private static int lowPressureSamples;
    private static long lastAdjustmentMillis;

    private static Integer originalRenderDistance;
    private static Integer originalSimulationDistance;
    private static int appliedRenderDistance = -1;
    private static int appliedSimulationDistance = -1;

    private static String statusKey = "screen.vulkanboost.memory.status.disabled";
    private static int pressurePercent;

    private SmartMemoryManager() {
    }

    public static void tick(Minecraft client) {
        if (++ticks < SAMPLE_INTERVAL_TICKS) {
            return;
        }
        ticks = 0;

        if (!VulkanBoostConfig.memoryAssistEnabled()) {
            restoreIfUnchanged(client);
            highPressureSamples = 0;
            lowPressureSamples = 0;
            statusKey = "screen.vulkanboost.memory.status.disabled";
            updatePressure();
            return;
        }

        if (client.level == null) {
            statusKey = "screen.vulkanboost.memory.status.waiting";
            updatePressure();
            return;
        }

        updatePressure();
        int threshold = VulkanBoostConfig.memoryThresholdPercent();

        if (hasIntervention()) {
            if (!stillAtAppliedValues(client)) {
                clearIntervention();
                statusKey = "screen.vulkanboost.memory.status.manual";
                lowPressureSamples = 0;
                return;
            }

            if (pressurePercent <= Math.max(60, threshold - 20)) {
                lowPressureSamples++;
                if (lowPressureSamples >= REQUIRED_LOW_SAMPLES) {
                    restoreIfUnchanged(client);
                    statusKey = "screen.vulkanboost.memory.status.restored";
                    lowPressureSamples = 0;
                    highPressureSamples = 0;
                } else {
                    statusKey = "screen.vulkanboost.memory.status.reduced";
                }
            } else {
                lowPressureSamples = 0;
                statusKey = "screen.vulkanboost.memory.status.reduced";
            }
            return;
        }

        if (pressurePercent >= threshold) {
            highPressureSamples++;
            statusKey = "screen.vulkanboost.memory.status.high";
        } else {
            highPressureSamples = 0;
            statusKey = "screen.vulkanboost.memory.status.observing";
            return;
        }

        if (highPressureSamples >= REQUIRED_HIGH_SAMPLES &&
                System.currentTimeMillis() - lastAdjustmentMillis >= ADJUSTMENT_COOLDOWN_MS) {
            reduceChunkDistances(client);
        }
    }

    public static int pressurePercent() {
        return pressurePercent;
    }

    public static String statusKey() {
        return statusKey;
    }

    private static void updatePressure() {
        Runtime runtime = Runtime.getRuntime();
        long max = runtime.maxMemory();
        long used = runtime.totalMemory() - runtime.freeMemory();
        pressurePercent = max <= 0 ? 0 : (int) Math.min(100, (used * 100L) / max);
    }

    private static void reduceChunkDistances(Minecraft client) {
        OptionalInt render = MinecraftOptionAccess.read(client, "renderDistance");
        OptionalInt simulation = MinecraftOptionAccess.read(client, "simulationDistance");
        if (render.isEmpty() || simulation.isEmpty()) {
            statusKey = "screen.vulkanboost.memory.status.api_unavailable";
            return;
        }

        int nextRender = Math.max(4, render.getAsInt() - 2);
        int nextSimulation = Math.max(2, simulation.getAsInt() - 1);
        if (nextRender == render.getAsInt() && nextSimulation == simulation.getAsInt()) {
            statusKey = "screen.vulkanboost.memory.status.minimum";
            return;
        }

        try {
            originalRenderDistance = render.getAsInt();
            originalSimulationDistance = simulation.getAsInt();

            if (!MinecraftOptionAccess.write(client, "renderDistance", nextRender) ||
                    !MinecraftOptionAccess.write(client, "simulationDistance", nextSimulation)) {
                restoreOriginalValues(client);
                statusKey = "screen.vulkanboost.memory.status.api_unavailable";
                return;
            }

            MinecraftOptionAccess.save(client);
            appliedRenderDistance = nextRender;
            appliedSimulationDistance = nextSimulation;
            highPressureSamples = 0;
            lowPressureSamples = 0;
            lastAdjustmentMillis = System.currentTimeMillis();
            statusKey = "screen.vulkanboost.memory.status.reduced";
            LOGGER.info("Memory assistance reduced render distance {}→{} and simulation distance {}→{} at {}% Java heap pressure.",
                    render.getAsInt(), nextRender, simulation.getAsInt(), nextSimulation, pressurePercent);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            restoreOriginalValues(client);
            statusKey = "screen.vulkanboost.memory.status.error";
            LOGGER.warn("Could not apply safe memory assistance.", exception);
        }
    }

    private static boolean hasIntervention() {
        return originalRenderDistance != null && originalSimulationDistance != null;
    }

    private static boolean stillAtAppliedValues(Minecraft client) {
        OptionalInt render = MinecraftOptionAccess.read(client, "renderDistance");
        OptionalInt simulation = MinecraftOptionAccess.read(client, "simulationDistance");
        return render.isPresent() && simulation.isPresent() &&
                render.getAsInt() == appliedRenderDistance &&
                simulation.getAsInt() == appliedSimulationDistance;
    }

    private static void restoreIfUnchanged(Minecraft client) {
        if (!hasIntervention()) {
            return;
        }
        if (stillAtAppliedValues(client)) {
            try {
                restoreOriginalValues(client);
                statusKey = "screen.vulkanboost.memory.status.restored";
            } catch (RuntimeException exception) {
                statusKey = "screen.vulkanboost.memory.status.error";
                LOGGER.warn("Could not restore the previous distance settings.", exception);
            }
        } else {
            clearIntervention();
            statusKey = "screen.vulkanboost.memory.status.manual";
        }
    }

    private static void restoreOriginalValues(Minecraft client) {
        try {
            if (originalRenderDistance != null) {
                MinecraftOptionAccess.write(client, "renderDistance", originalRenderDistance);
            }
            if (originalSimulationDistance != null) {
                MinecraftOptionAccess.write(client, "simulationDistance", originalSimulationDistance);
            }
            MinecraftOptionAccess.save(client);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not restore vanilla distance settings", exception);
        } finally {
            clearIntervention();
        }
    }

    private static void clearIntervention() {
        originalRenderDistance = null;
        originalSimulationDistance = null;
        appliedRenderDistance = -1;
        appliedSimulationDistance = -1;
    }
}
