package dev.vulkanboost.mixin;

import dev.zelo.renderscale.RenderScale;
import dev.zelo.renderscale.config.RenderScaleConfig;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VideoSettingsScreen.class)
public abstract class VideoSettingsScreenMixin {
    private static final float VULKANBOOST_IMAGE_SCALE = 0.77F;

    @Inject(method = "addOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/OptionsList;addHeader(Lnet/minecraft/network/chat/Component;)V", ordinal = 2))
    private void vulkanboost$addImageEnhancementOption(CallbackInfo callbackInfo) {
        RenderScaleConfig config = RenderScale.getConfig();
        boolean enabled = config.fsr && config.getScale() < 0.999F;

        OptionInstance<Boolean> option = OptionInstance.createBoolean(
                "options.vulkanboost.image_enhancement",
                OptionInstance.cachedConstantTooltip(
                        Component.translatable("options.vulkanboost.image_enhancement.tooltip")),
                OptionInstance.BOOLEAN_TO_STRING,
                enabled,
                VideoSettingsScreenMixin::vulkanboost$setImageEnhancement);

        OptionsList optionsList = ((OptionsSubScreenAccessor) (Object) this).getVulkanBoostList();
        optionsList.addHeader(Component.translatable("options.vulkanboost.category"));
        optionsList.addBig(option);
    }

    private static void vulkanboost$setImageEnhancement(boolean enabled) {
        RenderScaleConfig config = RenderScale.getConfig();
        config.scale = enabled ? VULKANBOOST_IMAGE_SCALE : 1.0F;
        config.fsr = enabled;
        // RenderScale's FSR 1 pipeline performs reconstruction in its own shader passes.
        RenderScale.CONFIG.save();
    }
}
