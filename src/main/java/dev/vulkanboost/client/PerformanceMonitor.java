package dev.vulkanboost.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

/**
 * Collects lightweight client metrics for the Control Center.
 * Sampling is throttled to once every ten client ticks.
 */
public final class PerformanceMonitor {
    private static final int SAMPLE_INTERVAL_TICKS = 10;
    private static int ticksUntilSample;

    private static volatile Snapshot latest = new Snapshot(0, 0L, 0L, 0L, 0L, 0);

    private PerformanceMonitor() {
    }

    public static void tick(Minecraft client) {
        if (++ticksUntilSample < SAMPLE_INTERVAL_TICKS) {
            return;
        }
        ticksUntilSample = 0;

        Runtime runtime = Runtime.getRuntime();
        long usedHeap = runtime.totalMemory() - runtime.freeMemory();
        long committedHeap = runtime.totalMemory();
        long maximumHeap = runtime.maxMemory();

        latest = new Snapshot(
                client.getFps(),
                client.getFrameTimeNs(),
                usedHeap,
                committedHeap,
                maximumHeap,
                FabricLoader.getInstance().getAllMods().size()
        );
    }

    public static Snapshot latest() {
        return latest;
    }

    public record Snapshot(
            int fps,
            long frameTimeNs,
            long usedHeapBytes,
            long committedHeapBytes,
            long maximumHeapBytes,
            int loadedComponentCount
    ) {
        public double frameTimeMillis() {
            return frameTimeNs > 0 ? frameTimeNs / 1_000_000.0 : 0.0;
        }
    }
}
