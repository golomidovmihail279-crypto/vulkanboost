package dev.vulkanboost.optimization;

import net.fabricmc.loader.api.FabricLoader;

import java.util.List;

/**
 * The complete inventory of bundled top-level components and their actual control support.
 * A component without a verified adapter is informational and cannot be toggled at runtime.
 */
public final class OptimizationRegistry {
    public record Entry(
            String id,
            String titleKey,
            String descriptionKey,
            String statusKey,
            OptimizationAdapter adapter
    ) {
        public boolean isInstalled() {
            return adapter != null
                    ? adapter.isInstalled()
                    : FabricLoader.getInstance().isModLoaded(id);
        }

        public boolean canToggle() {
            return adapter != null && adapter.isSupported() && isInstalled();
        }

        public boolean isEnabled() {
            return canToggle() && adapter.isEnabled();
        }

        public void setEnabled(boolean enabled) throws ReflectiveOperationException {
            if (!canToggle()) {
                throw new IllegalStateException("No verified configuration adapter for " + id);
            }
            adapter.setEnabled(enabled);
        }
    }

    private static final String READ_ONLY = "screen.vulkanboost.optimizations.status.read_only";
    private static final String LIBRARY = "screen.vulkanboost.optimizations.status.library";

    private static final List<Entry> ENTRIES = List.of(
            new Entry("fabric-api",
                    "screen.vulkanboost.optimizations.fabric_api",
                    "screen.vulkanboost.optimizations.fabric_api.description",
                    LIBRARY, null),
            new Entry("vulkanmod",
                    "screen.vulkanboost.optimizations.vulkanmod",
                    "screen.vulkanboost.optimizations.vulkanmod.description",
                    READ_ONLY, null),
            new Entry("lithium",
                    "screen.vulkanboost.optimizations.lithium",
                    "screen.vulkanboost.optimizations.lithium.description",
                    READ_ONLY, null),
            new Entry("ferritecore",
                    "screen.vulkanboost.optimizations.ferritecore",
                    "screen.vulkanboost.optimizations.ferritecore.description",
                    READ_ONLY, null),
            new Entry("alternate-current",
                    "screen.vulkanboost.optimizations.alternate_current",
                    "screen.vulkanboost.optimizations.alternate_current.description",
                    READ_ONLY, null),
            new Entry("badoptimizations",
                    "screen.vulkanboost.optimizations.badoptimizations",
                    "screen.vulkanboost.optimizations.badoptimizations.description",
                    READ_ONLY, null),
            new Entry("modernfix",
                    "screen.vulkanboost.optimizations.modernfix",
                    "screen.vulkanboost.optimizations.modernfix.description",
                    READ_ONLY, null),
            new Entry("scalablelux",
                    "screen.vulkanboost.optimizations.scalablelux",
                    "screen.vulkanboost.optimizations.scalablelux.description",
                    READ_ONLY, null),
            new Entry("zfastnoise",
                    "screen.vulkanboost.optimizations.fast_noise",
                    "screen.vulkanboost.optimizations.fast_noise.description",
                    READ_ONLY, null),
            new Entry("krypton",
                    "screen.vulkanboost.optimizations.krypton",
                    "screen.vulkanboost.optimizations.krypton.description",
                    READ_ONLY, null),
            new Entry("c2me",
                    "screen.vulkanboost.optimizations.c2me",
                    "screen.vulkanboost.optimizations.c2me.description",
                    READ_ONLY, null),
            new Entry("bura",
                    "screen.vulkanboost.optimizations.bura",
                    "screen.vulkanboost.optimizations.bura.description",
                    READ_ONLY, null),
            new Entry("epitaph-optimizer",
                    "screen.vulkanboost.optimizations.epitaph_optimizer",
                    "screen.vulkanboost.optimizations.epitaph_optimizer.description",
                    READ_ONLY, null),
            new Entry("dynamic_fps",
                    "screen.vulkanboost.optimizations.dynamic_fps",
                    "screen.vulkanboost.optimizations.dynamic_fps.description",
                    READ_ONLY,
                    new DynamicFpsAdapter()),
            new Entry("fastquit",
                    "screen.vulkanboost.optimizations.fastquit",
                    "screen.vulkanboost.optimizations.fastquit.description",
                    READ_ONLY, null),
            new Entry("smart_particles",
                    "screen.vulkanboost.optimizations.smart_particles",
                    "screen.vulkanboost.optimizations.smart_particles.description",
                    READ_ONLY, null),
            new Entry("cloth-config",
                    "screen.vulkanboost.optimizations.cloth_config",
                    "screen.vulkanboost.optimizations.cloth_config.description",
                    LIBRARY, null),
            new Entry("betterhurtcam",
                    "screen.vulkanboost.optimizations.betterhurtcam",
                    "screen.vulkanboost.optimizations.betterhurtcam.description",
                    READ_ONLY, null),
            new Entry("ukulib",
                    "screen.vulkanboost.optimizations.ukulib",
                    "screen.vulkanboost.optimizations.ukulib.description",
                    LIBRARY, null),
            new Entry("structure_layout_optimizer",
                    "screen.vulkanboost.optimizations.structure_layout",
                    "screen.vulkanboost.optimizations.structure_layout.description",
                    READ_ONLY, null),
            new Entry("resourcefulconfig",
                    "screen.vulkanboost.optimizations.resourceful_config",
                    "screen.vulkanboost.optimizations.resourceful_config.description",
                    LIBRARY, null),
            new Entry("cullleaves",
                    "screen.vulkanboost.optimizations.cullleaves",
                    "screen.vulkanboost.optimizations.cullleaves.description",
                    READ_ONLY,
                    new CullLeavesAdapter()),
            new Entry("midnightlib",
                    "screen.vulkanboost.optimizations.midnightlib",
                    "screen.vulkanboost.optimizations.midnightlib.description",
                    LIBRARY, null),
            new Entry("puzzleslib",
                    "screen.vulkanboost.optimizations.puzzleslib",
                    "screen.vulkanboost.optimizations.puzzleslib.description",
                    LIBRARY, null),
            new Entry("forgeconfigapiport",
                    "screen.vulkanboost.optimizations.forge_config_api_port",
                    "screen.vulkanboost.optimizations.forge_config_api_port.description",
                    LIBRARY, null)
    );

    private OptimizationRegistry() {
    }

    public static List<Entry> entries() {
        return ENTRIES;
    }
}
