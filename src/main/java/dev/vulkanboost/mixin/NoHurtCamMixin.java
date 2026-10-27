package dev.vulkanboost.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Disables only the vanilla damage camera tilt ("hurt cam").
 * Damage, knockback, hit feedback, and the rest of camera movement are unchanged.
 */
@Mixin(GameRenderer.class)
public abstract class NoHurtCamMixin {
    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void vulkanboost$disableHurtCamera(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        ci.cancel();
    }
}
