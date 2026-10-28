package dev.vulkanboost.mixin;

import dev.zelo.renderscale.RenderScale;
import dev.zelo.renderscale.config.RenderScaleConfig;
import net.minecraft.network.chat.Component;
import net.vulkanmod.config.gui.OptionBlock;
import net.vulkanmod.config.option.Option;
import net.vulkanmod.config.option.SwitchOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds a safe render-scale switch to VulkanMod's Graphics options.
 * FSR stays disabled until the 1.21.11 + VulkanMod output path is verified.
 */
@Mixin(targets = "net.vulkanmod.config.option.Options")
public abstract class VulkanModOptionsMixin {
    private static final float VULKANBOOST_IMAGE_SCALE = 0.77F;

    @Inject(method = "getGraphicsOpts", at = @At("RETURN"), cancellable = true)
    private static void vulkanboost$addImageEnhancementOption(
            CallbackInfoReturnable<OptionBlock[]> callbackInfo) {
        OptionBlock[] blocks = callbackInfo.getReturnValue().clone();
        if (blocks.length == 0) {
            return;
        }

        OptionBlock graphicsBlock = blocks[0];
        Option<?>[] original = graphicsBlock.options();
        int insertionIndex = Math.min(1, original.length);
        Option<?>[] updated = new Option<?>[original.length + 1];
        System.arraycopy(original, 0, updated, 0, insertionIndex);
        updated[insertionIndex] = vulkanboost$createImageEnhancementOption();
        System.arraycopy(original, insertionIndex, updated, insertionIndex + 1,
                original.length - insertionIndex);

        blocks[0] = new OptionBlock(graphicsBlock.title(), updated);
        callbackInfo.setReturnValue(blocks);
    }

    private static Option<Boolean> vulkanboost$createImageEnhancementOption() {
        return new SwitchOption(
                Component.translatable("options.vulkanboost.image_enhancement"),
                enabled -> {
                    RenderScaleConfig config = RenderScale.getConfig();
                    config.scale = enabled ? VULKANBOOST_IMAGE_SCALE : 1.0F;
                    // FSR currently produces a black world on this renderer combination.
                    // Keep the 77% performance scale but use RenderScale's normal linear blit.
                    config.fsr = false;
                    RenderScale.CONFIG.save();
                },
                () -> {
                    RenderScaleConfig config = RenderScale.getConfig();
                    return config.getScale() < 0.999F;
                }
        ).setTooltip(enabled -> Component.translatable(
                "options.vulkanboost.image_enhancement.tooltip"));
    }
}
