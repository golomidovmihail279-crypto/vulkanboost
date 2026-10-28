package dev.vulkanboost.config;

/**
 * Runtime state for the temporary EASU-only probe.
 *
 * This flag is deliberately not persisted: after a restart the safe linear
 * upscale path is restored even if a previous test left RenderScale's fsr
 * option enabled in its config file.
 */
public final class FsrEasuTestMode {
    private FsrEasuTestMode() {
    }

    public static volatile boolean enabled = false;
}
