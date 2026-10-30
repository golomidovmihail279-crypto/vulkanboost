package dev.vulkanboost.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import dev.vulkanboost.config.VulkanBoostConfig;
import dev.vulkanboost.client.PerformanceMonitor;

/**
 * Registers the VulkanBoost client entry point and Control Center shortcut.
 */
public final class VulkanBoostClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("vulkanboost", "main")
    );

    private static KeyMapping openControlCenterKey;

    @Override
    public void onInitializeClient() {
        VulkanBoostConfig.load();

        openControlCenterKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.vulkanboost.open_control_center",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            PerformanceMonitor.tick(client);
            while (openControlCenterKey.consumeClick()) {
                if (!(client.screen instanceof ControlCenterScreen)) {
                    client.setScreen(new ControlCenterScreen(client.screen));
                }
            }
        });
    }
}
