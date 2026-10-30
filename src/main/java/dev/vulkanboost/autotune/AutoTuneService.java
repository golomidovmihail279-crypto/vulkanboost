package dev.vulkanboost.autotune;

import dev.vulkanboost.optimization.OptimizationRegistry;
import net.minecraft.client.Minecraft;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Builds explicit, reversible profiles from reliable client/runtime facts.
 * The tuner only touches vanilla distance options and verified mod adapters.
 */
public final class AutoTuneService {
    public enum Profile {
        BALANCED("screen.vulkanboost.autotune.profile.balanced", 12, 8),
        FPS_PRIORITY("screen.vulkanboost.autotune.profile.fps", 8, 6),
        LOW_MEMORY("screen.vulkanboost.autotune.profile.memory", 6, 4);

        private final String titleKey;
        private final int renderDistance;
        private final int simulationDistance;

        Profile(String titleKey, int renderDistance, int simulationDistance) {
            this.titleKey = titleKey;
            this.renderDistance = renderDistance;
            this.simulationDistance = simulationDistance;
        }

        public String titleKey() {
            return titleKey;
        }
    }

    public record Plan(
            Profile profile,
            int logicalProcessors,
            long maxHeapBytes,
            int currentRenderDistance,
            int targetRenderDistance,
            int currentSimulationDistance,
            int targetSimulationDistance,
            Map<String, Boolean> featureTargets
    ) {
    }

    private record Snapshot(
            Integer renderDistance,
            Integer simulationDistance,
            Map<String, Boolean> featureValues
    ) {
    }

    private static Snapshot lastAppliedSnapshot;

    private AutoTuneService() {
    }

    public static Profile recommendedProfile() {
        Runtime runtime = Runtime.getRuntime();
        int processors = runtime.availableProcessors();
        long maxHeap = runtime.maxMemory();

        if (processors <= 6 || maxHeap <= 6L * 1024L * 1024L * 1024L) {
            return Profile.LOW_MEMORY;
        }
        return Profile.BALANCED;
    }

    public static Plan preview(Minecraft client, Profile profile) {
        Runtime runtime = Runtime.getRuntime();
        OptionalInt render = MinecraftOptionAccess.read(client, "renderDistance");
        OptionalInt simulation = MinecraftOptionAccess.read(client, "simulationDistance");

        int currentRender = render.orElse(-1);
        int currentSimulation = simulation.orElse(-1);
        int targetRender = currentRender < 0
                ? -1
                : Math.min(currentRender, profile.renderDistance);
        int targetSimulation = currentSimulation < 0
                ? -1
                : Math.min(currentSimulation, profile.simulationDistance);

        Map<String, Boolean> featureTargets = new LinkedHashMap<>();
        for (OptimizationRegistry.Entry entry : OptimizationRegistry.entries()) {
            if (entry.canToggle() &&
                    (entry.id().equals("cullleaves") || entry.id().equals("dynamic_fps"))) {
                featureTargets.put(entry.id(), true);
            }
        }

        return new Plan(profile, runtime.availableProcessors(), runtime.maxMemory(),
                currentRender, targetRender, currentSimulation, targetSimulation,
                Map.copyOf(featureTargets));
    }

    public static void apply(Minecraft client, Plan plan) throws ReflectiveOperationException {
        Map<String, Boolean> oldFeatures = new LinkedHashMap<>();
        for (OptimizationRegistry.Entry entry : OptimizationRegistry.entries()) {
            if (plan.featureTargets().containsKey(entry.id()) && entry.canToggle()) {
                oldFeatures.put(entry.id(), entry.isEnabled());
            }
        }

        Snapshot before = new Snapshot(
                plan.currentRenderDistance() >= 0 ? plan.currentRenderDistance() : null,
                plan.currentSimulationDistance() >= 0 ? plan.currentSimulationDistance() : null,
                Map.copyOf(oldFeatures)
        );

        try {
            if (plan.targetRenderDistance() >= 0) {
                MinecraftOptionAccess.write(client, "renderDistance", plan.targetRenderDistance());
            }
            if (plan.targetSimulationDistance() >= 0) {
                MinecraftOptionAccess.write(client, "simulationDistance", plan.targetSimulationDistance());
            }

            for (OptimizationRegistry.Entry entry : OptimizationRegistry.entries()) {
                Boolean target = plan.featureTargets().get(entry.id());
                if (target != null && entry.canToggle() && entry.isEnabled() != target) {
                    entry.setEnabled(target);
                }
            }

            MinecraftOptionAccess.save(client);
            lastAppliedSnapshot = before;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            restore(client, before);
            throw exception;
        }
    }

    public static boolean canUndo() {
        return lastAppliedSnapshot != null;
    }

    public static void undo(Minecraft client) throws ReflectiveOperationException {
        if (lastAppliedSnapshot == null) {
            return;
        }
        Snapshot snapshot = lastAppliedSnapshot;
        restore(client, snapshot);
        lastAppliedSnapshot = null;
    }

    private static void restore(Minecraft client, Snapshot snapshot) throws ReflectiveOperationException {
        if (snapshot.renderDistance() != null) {
            MinecraftOptionAccess.write(client, "renderDistance", snapshot.renderDistance());
        }
        if (snapshot.simulationDistance() != null) {
            MinecraftOptionAccess.write(client, "simulationDistance", snapshot.simulationDistance());
        }

        for (OptimizationRegistry.Entry entry : OptimizationRegistry.entries()) {
            Boolean original = snapshot.featureValues().get(entry.id());
            if (original != null && entry.canToggle() && entry.isEnabled() != original) {
                entry.setEnabled(original);
            }
        }
        MinecraftOptionAccess.save(client);
    }
}
