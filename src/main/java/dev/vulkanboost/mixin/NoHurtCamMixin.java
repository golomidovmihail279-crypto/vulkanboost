package dev.vulkanboost.mixin;

import dev.vulkanboost.NoHurtCamConfig;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Reduces vanilla hurt-camera rotation by the configured percentage.
 * 0% reduction preserves vanilla behavior; 100% reduction removes the hurt-camera tilt.
 */
@Mixin(GameRenderer.class)
public abstract class NoHurtCamMixin {
    @ModifyArg(
        method = "bobHurt",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V"
        ),
        index = 0
    )
    private Quaternionfc vulkanboost$scaleHurtCamera(Quaternionfc rotation) {
        if (!NoHurtCamConfig.isEnabled()) {
            return rotation;
        }

        float rotationScale = 1.0F - (NoHurtCamConfig.getReduction() / 100.0F);
        if (rotationScale <= 0.0001F) {
            return new Quaternionf().identity();
        }
        if (rotationScale >= 0.9999F) {
            return rotation;
        }
        return new Quaternionf().slerp(rotation, rotationScale);
    }
}
