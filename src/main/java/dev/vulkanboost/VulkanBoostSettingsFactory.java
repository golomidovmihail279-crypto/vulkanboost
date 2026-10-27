package dev.vulkanboost;

import dev.zelo.renderscale.RenderScale;
import dev.zelo.renderscale.config.RenderScaleConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.vulkanmod.config.api.VkModSettingsEntryBuilder;
import net.vulkanmod.config.api.VkModSettingsFactory;
import net.vulkanmod.config.gui.ModSettingsEntry;
import net.vulkanmod.config.option.SwitchOption;

/**
 * Registers VulkanBoost's upscaling toggle in VulkanMod's own settings UI.
 *
 * VulkanMod replaces the vanilla Options screen with VOptionScreen, so a mixin
 * into vanilla VideoSettingsScreen does not expose this option in the usual UI.
 */
public final class VulkanBoostSettingsFactory implements VkModSettingsFactory {
    private static final float IMAGE_ENHANCEMENT_SCALE = 0.77F;

    @Override
    public ModSettingsEntry build(VkModSettingsEntryBuilder builder) {
        var settings = builder
                .setModName(Component.literal("VulkanBoost"))
                .setIcon(Identifier.fromNamespaceAndPath("vulkanmod", "vlogo_transparent.png"))
                .setOnApply(() -> RenderScale.CONFIG.save());

        settings.withPage("FSR 1")
                .withOptionBlock("")
                .addOption(new SwitchOption(
                        Component.translatable("options.vulkanboost.image_enhancement"),
                        enabled -> {
                            RenderScaleConfig config = RenderScale.getConfig();
                            config.scale = enabled ? IMAGE_ENHANCEMENT_SCALE : 1.0F;
                            config.fsr = enabled;
                        },
                        () -> {
                            RenderScaleConfig config = RenderScale.getConfig();
                            return config.fsr && config.getScale() < 0.999F;
                        }
                ).setTooltip(enabled -> Component.translatable("options.vulkanboost.image_enhancement.tooltip")));

        return settings.build();
    }
}
