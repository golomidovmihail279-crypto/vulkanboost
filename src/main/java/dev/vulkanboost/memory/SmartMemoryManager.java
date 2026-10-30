package dev.vulkanboost.memory;

import dev.vulkanboost.autotune.AutoTuneService;
import dev.vulkanboost.autotune.MinecraftOptionAccess;
import dev.vulkanboost.autotune.ReversibleOptionChanges;
import dev.vulkanboost.config.VulkanBoostConfig;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
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

    private static ReversibleOptionChanges.Snapshot intervention;

    private static String statusKey = "screen.vulkanboost.memory.status.disabled";
    private static int pressurePercent;

    private SmartMemoryManager() {
    }

    public static void tick(Minecraft client) {
        if (++ticks < SAMPLE_INTERVAL_TICKS) {
            return;
        }
        ticks = 0;
        updatePressure();

        // Auto-tune and memory assistance both own reversible distance snapshots.
        // Keep the monitor fresh, but do not let the memory helper interpret an
        // active profile as a manual override. Resume after the profile is undone.
        if (AutoTuneService.canUndo()) {
            statusKey = "screen.vulkanboost.memory.status.profile_active";
            return;
        }

        if (!VulkanBoostConfig.memoryAssistEnabled()) {
            if (!restoreIfUnchanged(client)) {
                return;
            }
            highPressureSamples = 0;
            lowPressureSamples = 0;
            statusKey = "screen.vulkanboost.memory.status.disabled";
            return;
        }

        if (client.level == null) {
            if (!restoreIfUnchanged(client)) {
                return;
            }
            statusKey = "screen.vulkanboost.memory.status.waiting";
            return;
        }

        int threshold = VulkanBoostConfig.memoryThresholdPercent();

        if (hasIntervention()) {
            ReversibleOptionChanges.Access access = MinecraftOptionAccess.bind(client);
            if (!ReversibleOptionChanges.stillAtAppliedValues(access, intervention)) {
                restoreIfUnchanged(client);
                highPressureSamples = 0;
                lowPressureSamples = 0;
                return;
            }

            if (pressurePercent <= Math.max(60, threshold - 20)) {
                lowPressureSamples++;
                if (lowPressureSamples >= REQUIRED_LOW_SAMPLES) {
                    restoreIfUnchanged(client);
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
        pressurePercent = max <= 0
                ? 0
                : (int) Math.min(100.0, ((double) used / (double) max) * 100.0);
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
            highPressureSamples = 0;
            lastAdjustmentMillis = System.currentTimeMillis();
            return;
        }

        Map<String, Integer> targets = new LinkedHashMap<>();
        targets.put("renderDistance", nextRender);
        targets.put("simulationDistance", nextSimulation);

        try {
            ReversibleOptionChanges.Snapshot snapshot = ReversibleOptionChanges.apply(
                    MinecraftOptionAccess.bind(client), targets, true
            );
            if (snapshot.isEmpty()) {
                statusKey = "screen.vulkanboost.memory.status.minimum";
                highPressureSamples = 0;
                lastAdjustmentMillis = System.currentTimeMillis();
                return;
            }

            intervention = snapshot;
            highPressureSamples = 0;
            lowPressureSamples = 0;
            lastAdjustmentMillis = System.currentTimeMillis();
            statusKey = "screen.vulkanboost.memory.status.reduced";
            LOGGER.info("Memory assistance reduced render distance {}→{} and simulation distance {}→{} at {}% Java heap pressure.",
                    render.getAsInt(), nextRender, simulation.getAsInt(), nextSimulation, pressurePercent);
        } catch (ReversibleOptionChanges.ApplyException exception) {
            if (!exception.rollbackComplete()) {
                intervention = exception.snapshot();
            }
            statusKey = "screen.vulkanboost.memory.status.error";
            LOGGER.warn("Could not apply memory assistance. Rollback complete: {}.",
                    exception.rollbackComplete(), exception);
        } catch (RuntimeException exception) {
            statusKey = "screen.vulkanboost.memory.status.error";
            LOGGER.warn("Could not apply memory assistance.", exception);
        }
    }

    private static boolean hasIntervention() {
        return intervention != null && !intervention.isEmpty();
    }

    private static boolean restoreIfUnchanged(Minecraft client) {
        if (!hasIntervention()) {
            intervention = null;
            return true;
        }

        try {
            ReversibleOptionChanges.RestoreResult result =
                    ReversibleOptionChanges.restoreIfUnchanged(
                            MinecraftOptionAccess.bind(client), intervention
                    );
            if (!result.complete()) {
                statusKey = "screen.vulkanboost.memory.status.error";
                LOGGER.warn("Memory-assistance restoration is pending; option values are not currently readable or writable.");
                return false;
            }

            intervention = null;
            statusKey = result.preservedValues() > 0
                    ? "screen.vulkanboost.memory.status.manual"
                    : "screen.vulkanboost.memory.status.restored";
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            statusKey = "screen.vulkanboost.memory.status.error";
            LOGGER.warn("Could not restore the previous distance settings. Restoration will be retried.", exception);
            return false;
        }
    }
}
