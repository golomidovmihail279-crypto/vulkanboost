package dev.vulkanboost.mixin;

import dev.zelo.renderscale.RenderScale;
import dev.zelo.renderscale.config.RenderScaleConfig;
import net.minecraft.network.chat.Component;
import net.vulkanmod.config.gui.OptionBlock;
import net.vulkanmod.config.option.Option;
import net.vulkanmod.config.option.OptionPage;
import net.vulkanmod.config.option.SwitchOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds a VulkanBoost page to VulkanMod's own settings UI using the option-page
 * list already consumed by the bundled VulkanMod version. This avoids relying
 * on a settings-registry API that is absent from the pinned release JAR.
 */
@Mixin(targets = "net.vulkanmod.config.option.Options")
public abstract class VulkanModOptionsMixin {
    private static final float VULKANBOOST_IMAGE_SCALE = 0.77F;

    @Inject(method = "getOptionPages", at = @At("RETURN"), cancellable = true)
    private static void vulkanboost$addImageEnhancementPage(
            CallbackInfoReturnable<List<OptionPage>> callbackInfo) {
        List<OptionPage> pages = new ArrayList<>(callbackInfo.getReturnValue());

        SwitchOption imageEnhancement = new SwitchOption(
                Component.translatable("options.vulkanboost.image_enhancement"),
                enabled -> {
                    RenderScaleConfig config = RenderScale.getConfig();
                    config.scale = enabled ? VULKANBOOST_IMAGE_SCALE : 1.0F;
                    config.fsr = enabled;
                    RenderScale.CONFIG.save();
                },
                () -> {
                    RenderScaleConfig config = RenderScale.getConfig();
                    return config.fsr && config.getScale() < 0.999F;
                }
        ).setTooltip(enabled -> Component.translatable(
                "options.vulkanboost.image_enhancement.tooltip"));

        pages.add(new OptionPage(
                "VulkanBoost",
                new OptionBlock[] {
                        new OptionBlock("", new Option<?>[] { imageEnhancement })
                }
        ));

        callbackInfo.setReturnValue(pages);
    }
}
