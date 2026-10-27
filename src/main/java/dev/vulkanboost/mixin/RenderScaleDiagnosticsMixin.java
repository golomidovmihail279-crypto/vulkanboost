package dev.vulkanboost.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.logging.LogUtils;
import dev.zelo.renderscale.RenderScale;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Emits one diagnostic per client session to show whether RenderScale enters
 * its FSR branch and reaches the EASU and RCAS draw calls.
 *
 * This observes the upstream RenderScale implementation without modifying its JAR.
 */
@Mixin(targets = "dev.zelo.renderscale.RenderScale")
public abstract class RenderScaleDiagnosticsMixin {
    @Unique
    private static final Logger VULKANBOOST$LOGGER = LogUtils.getLogger();

    @Unique
    private static boolean vulkanboost$loggedFsrEntry;

    @Unique
    private static boolean vulkanboost$loggedEasuDraw;

    @Unique
    private static boolean vulkanboost$loggedRcasDraw;

    @Inject(method = "blitAndBlendToTexture", at = @At("HEAD"), require = 1)
    private void vulkanboost$logFsrEntry(
            RenderTarget input, RenderTarget output, FilterMode filter, CallbackInfo ci) {
        if (!RenderScale.getConfig().fsr || vulkanboost$loggedFsrEntry) {
            return;
        }

        vulkanboost$loggedFsrEntry = true;
        VULKANBOOST$LOGGER.info(
                "[VulkanBoost FSR diagnostic] Entered FSR branch: input={}x{}, output={}x{}, scale={}, filter={}",
                input.width, input.height, output.width, output.height,
                RenderScale.getConfig().getScale(), filter);
    }

    @Inject(
            method = "blitAndBlendToTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderPass;draw(IIII)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER),
            require = 1)
    private void vulkanboost$logEasuDraw(
            RenderTarget input, RenderTarget output, FilterMode filter, CallbackInfo ci) {
        if (RenderScale.getConfig().fsr && !vulkanboost$loggedEasuDraw) {
            vulkanboost$loggedEasuDraw = true;
            VULKANBOOST$LOGGER.info(
                    "[VulkanBoost FSR diagnostic] EASU draw command issued.");
        }
    }

    @Inject(
            method = "blitAndBlendToTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderPass;draw(IIII)V",
                    ordinal = 1,
                    shift = At.Shift.AFTER),
            require = 1)
    private void vulkanboost$logRcasDraw(
            RenderTarget input, RenderTarget output, FilterMode filter, CallbackInfo ci) {
        if (RenderScale.getConfig().fsr && !vulkanboost$loggedRcasDraw) {
            vulkanboost$loggedRcasDraw = true;
            VULKANBOOST$LOGGER.info(
                    "[VulkanBoost FSR diagnostic] RCAS draw command issued.");
        }
    }
}
