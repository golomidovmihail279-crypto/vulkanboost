package dev.vulkanboost;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class VulkanBoostClient implements ClientModInitializer {
    private static final KeyMapping OPEN_NO_HURT_CAM = KeyBindingHelper.registerKeyBinding(
        new KeyMapping(
            "key.vulkanboost.open_no_hurt_cam",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            KeyMapping.Category.MISC
        )
    );

    @Override
    public void onInitializeClient() {
        NoHurtCamConfig.load();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_NO_HURT_CAM.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new NoHurtCamScreen(null));
                }
            }
        });
    }
}
