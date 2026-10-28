package dev.vulkanboost.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.logging.LogUtils;
import dev.zelo.renderscale.RenderScale;
import dev.zelo.renderscale.config.RenderScaleConfig;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Protects the 1.21.11 + VulkanMod combination from the experimental RenderScale
 * FSR path, which reached its draw calls but produced a black world in runtime tests.
 */
@Mixin(targets = "dev.zelo.renderscale.RenderScale")
public abstract class RenderScaleDiagnosticsMixin {
    @Unique
    private static final Logger VULKANBOOST$LOGGER = LogUtils.getLogger();

    @Unique
    private static boolean vulkanboost$loggedSafeFallback;

    @Inject(method = "blitAndBlendToTexture", at = @At("HEAD"), require = 1)
    private void vulkanboost$disableBlackFsrPath(
            RenderTarget input, RenderTarget output, FilterMode filter, CallbackInfo ci) {
        RenderScaleConfig config = RenderScale.getConfig();
        if (!config.fsr) {
            return;
        }

        config.fsr = false;
        RenderScale.CONFIG.save();
        if (!vulkanboost$loggedSafeFallback) {
            vulkanboost$loggedSafeFallback = true;
            VULKANBOOST$LOGGER.warn(
                    "[VulkanBoost] Disabled RenderScale FSR: its draw calls produced a black world on Minecraft 1.21.11 + VulkanMod. Falling back to RenderScale's normal blit at scale {}.",
                    config.getScale());
        }
    }
}
