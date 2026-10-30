package dev.vulkanboost.optimization;

/**
 * A verified bridge to a bundled optimization mod's own configuration.
 * Implementations must not report a feature as toggleable unless every
 * required runtime method is present.
 */
public interface OptimizationAdapter {
    String id();

    boolean isInstalled();

    boolean isSupported();

    boolean isEnabled();

    void setEnabled(boolean enabled) throws ReflectiveOperationException;
}
