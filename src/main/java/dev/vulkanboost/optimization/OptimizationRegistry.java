package dev.vulkanboost.optimization;

import net.fabricmc.loader.api.FabricLoader;

import java.util.List;

/**
 * Central inventory of optimization components and their actual control support.
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

    private static final List<Entry> ENTRIES = List.of(
            new Entry("cullleaves",
                    "screen.vulkanboost.optimizations.cullleaves",
                    "screen.vulkanboost.optimizations.cullleaves.description",
                    "screen.vulkanboost.optimizations.status.toggleable",
                    new CullLeavesAdapter()),
            new Entry("dynamic_fps",
                    "screen.vulkanboost.optimizations.dynamic_fps",
                    "screen.vulkanboost.optimizations.dynamic_fps.description",
                    "screen.vulkanboost.optimizations.status.toggleable",
                    new DynamicFpsAdapter()),
            new Entry("lithium",
                    "screen.vulkanboost.optimizations.lithium",
                    "screen.vulkanboost.optimizations.lithium.description",
                    "screen.vulkanboost.optimizations.status.restart_unsupported",
                    null),
            new Entry("structure_layout_optimizer",
                    "screen.vulkanboost.optimizations.structure_layout",
                    "screen.vulkanboost.optimizations.structure_layout.description",
                    "screen.vulkanboost.optimizations.status.no_adapter",
                    null),
            new Entry("modernfix",
                    "screen.vulkanboost.optimizations.modernfix",
                    "screen.vulkanboost.optimizations.modernfix.description",
                    "screen.vulkanboost.optimizations.status.no_adapter",
                    null)
    );

    private OptimizationRegistry() {
    }

    public static List<Entry> entries() {
        return ENTRIES;
    }
}
