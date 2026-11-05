package dev.vulkanboost.memory;

/**
 * Pure policy for reducing distance settings under sustained heap pressure.
 * The result is never greater than the user's current value, even when that
 * value is already below the preferred soft minimum.
 */
public final class MemoryDistancePolicy {
    private MemoryDistancePolicy() {
    }

    public static int reduce(int current, int amount, int softMinimum) {
        if (current < 0 || amount < 0 || softMinimum < 0) {
            throw new IllegalArgumentException("Distance values must be non-negative.");
        }
        return Math.min(current, Math.max(softMinimum, current - amount));
    }
}
