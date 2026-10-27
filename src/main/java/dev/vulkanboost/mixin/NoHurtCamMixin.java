package dev.vulkanboost.mixin;

import dev.vulkanboost.NoHurtCamConfig;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Adjusts only the hurt-tilt angle in GameRenderer.bobHurt.
 *
 * Minecraft 1.21.11 applies the directional damage tilt at the third call to
 * Axis.rotationDegrees in bobHurt (ordinal 2): the first call is the death roll,
 * and the other two are the yaw rotations around the tilt. Modifying this one
 * angle avoids interfering with those unrelated rotations.
 */
@Mixin(GameRenderer.class)
public abstract class NoHurtCamMixin {
    @ModifyArg(
        method = "bobHurt",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/math/Axis;rotationDegrees(F)Lorg/joml/Quaternionf;",
            ordinal = 2
        ),
        index = 0,
        require = 1
    )
    private float vulkanboost$scaleHurtCamera(float vanillaDegrees) {
        if (!NoHurtCamConfig.isEnabled()) {
            return vanillaDegrees;
        }

        float rotationScale = 1.0F - (NoHurtCamConfig.getReduction() / 100.0F);
        return vanillaDegrees * rotationScale;
    }
}
