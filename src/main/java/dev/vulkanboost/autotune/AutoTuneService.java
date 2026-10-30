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
            Map<String, Boolean> featureTargets,
            Map<String, Boolean> currentFeatureValues
    ) {
    }

    private record Snapshot(
            ReversibleOptionChanges.Snapshot distanceChanges,
            Map<String, Boolean> previousFeatureValues,
            Map<String, Boolean> appliedFeatureValues
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
        Map<String, Boolean> currentFeatureValues = new LinkedHashMap<>();
        for (OptimizationRegistry.Entry entry : OptimizationRegistry.entries()) {
            if (entry.canToggle() &&
                    (entry.id().equals("cullleaves") || entry.id().equals("dynamic_fps"))) {
                featureTargets.put(entry.id(), true);
                currentFeatureValues.put(entry.id(), entry.isEnabled());
            }
        }

        return new Plan(profile, runtime.availableProcessors(), runtime.maxMemory(),
                currentRender, targetRender, currentSimulation, targetSimulation,
                Map.copyOf(featureTargets), Map.copyOf(currentFeatureValues));
    }

    public static void apply(Minecraft client, Plan plan) {
        verifyPreviewIsCurrent(client, "renderDistance", plan.currentRenderDistance());
        verifyPreviewIsCurrent(client, "simulationDistance", plan.currentSimulationDistance());

        for (Map.Entry<String, Boolean> expected : plan.currentFeatureValues().entrySet()) {
            OptimizationRegistry.Entry entry = findEntry(expected.getKey());
            if (!entry.canToggle() || entry.isEnabled() != expected.getValue()) {
                throw new IllegalStateException("Optimization settings changed after the preview. Preview the profile again.");
            }
        }

        Map<String, Integer> distanceTargets = new LinkedHashMap<>();
        if (plan.targetRenderDistance() >= 0) {
            distanceTargets.put("renderDistance", plan.targetRenderDistance());
        }
        if (plan.targetSimulationDistance() >= 0) {
            distanceTargets.put("simulationDistance", plan.targetSimulationDistance());
        }

        ReversibleOptionChanges.Snapshot distanceChanges;
        try {
            distanceChanges = ReversibleOptionChanges.apply(
                    MinecraftOptionAccess.bind(client), distanceTargets, false
            );
        } catch (ReversibleOptionChanges.ApplyException exception) {
            if (!exception.rollbackComplete()) {
                lastAppliedSnapshot = new Snapshot(exception.snapshot(), Map.of(), Map.of());
            }
            throw exception;
        }

        Map<String, Boolean> previousFeatures = new LinkedHashMap<>();
        Map<String, Boolean> appliedFeatures = new LinkedHashMap<>();
        try {
            for (Map.Entry<String, Boolean> target : plan.featureTargets().entrySet()) {
                OptimizationRegistry.Entry entry = findEntry(target.getKey());
                if (!entry.canToggle()) {
                    throw new IllegalStateException("Optimization adapter is no longer available: " + target.getKey());
                }

                boolean previous = entry.isEnabled();
                previousFeatures.put(target.getKey(), previous);
                appliedFeatures.put(target.getKey(), target.getValue());

                if (previous != target.getValue()) {
                    entry.setEnabled(target.getValue());
                }
            }

            lastAppliedSnapshot = new Snapshot(
                    distanceChanges,
                    Map.copyOf(previousFeatures),
                    Map.copyOf(appliedFeatures)
            );
        } catch (ReflectiveOperationException | RuntimeException failure) {
            Snapshot failedAttempt = new Snapshot(
                    distanceChanges,
                    Map.copyOf(previousFeatures),
                    Map.copyOf(appliedFeatures)
            );
            try {
                restore(client, failedAttempt);
            } catch (ReflectiveOperationException | RuntimeException rollbackFailure) {
                lastAppliedSnapshot = failedAttempt;
                failure.addSuppressed(rollbackFailure);
            }
            throw failure;
        }
    }

    public static boolean canUndo() {
        return lastAppliedSnapshot != null;
    }

    public static void undo(Minecraft client) throws ReflectiveOperationException {
        if (lastAppliedSnapshot == null) {
            return;
        }
        restore(client, lastAppliedSnapshot);
        lastAppliedSnapshot = null;
    }

    private static void verifyPreviewIsCurrent(Minecraft client, String optionName, int expected) {
        if (expected < 0) {
            return;
        }

        OptionalInt current = MinecraftOptionAccess.read(client, optionName);
        if (current.isEmpty() || current.getAsInt() != expected) {
            throw new IllegalStateException("Minecraft settings changed after the preview. Preview the profile again.");
        }
    }

    private static OptimizationRegistry.Entry findEntry(String id) {
        return OptimizationRegistry.entries().stream()
                .filter(entry -> entry.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unknown optimization adapter: " + id));
    }

    private static void restore(Minecraft client, Snapshot snapshot) throws ReflectiveOperationException {
        ReversibleOptionChanges.RestoreResult distanceResult =
                ReversibleOptionChanges.restoreIfUnchanged(
                        MinecraftOptionAccess.bind(client), snapshot.distanceChanges()
                );
        if (!distanceResult.complete()) {
            throw new IllegalStateException("Distance settings could not be restored yet.");
        }

        for (OptimizationRegistry.Entry entry : OptimizationRegistry.entries()) {
            Boolean original = snapshot.previousFeatureValues().get(entry.id());
            Boolean applied = snapshot.appliedFeatureValues().get(entry.id());
            if (original == null || applied == null) {
                continue;
            }
            if (!entry.canToggle()) {
                throw new IllegalStateException("Optimization adapter is no longer available: " + entry.id());
            }

            boolean current = entry.isEnabled();
            if (current == applied && current != original) {
                entry.setEnabled(original);
            }
            // If current differs from the value applied by VulkanBoost, preserve
            // it: the user or another mod has changed the setting since then.
        }
    }
}
