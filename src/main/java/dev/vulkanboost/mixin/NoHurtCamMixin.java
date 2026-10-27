package dev.vulkanboost.mixin;

import dev.vulkanboost.NoHurtCamConfig;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Scales vanilla hurt-camera rotations using the NoHurtCam slider.
 * 0% preserves vanilla behavior; 100% removes the hurt-camera tilt.
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
