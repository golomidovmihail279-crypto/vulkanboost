package dev.vulkanboost;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public final class VulkanBoostClient implements ClientModInitializer {
    private static final KeyMapping.Category VULKANBOOST_CATEGORY =
        KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath("vulkanboost", "controls"));

    private static final KeyMapping TOGGLE_NO_HURT_CAM = KeyBindingHelper.registerKeyBinding(
        new KeyMapping(
            "key.vulkanboost.toggle_no_hurt_cam",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            VULKANBOOST_CATEGORY
        )
    );

    private static final KeyMapping OPEN_NO_HURT_CAM_SETTINGS = KeyBindingHelper.registerKeyBinding(
        new KeyMapping(
            "key.vulkanboost.open_no_hurt_cam",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            VULKANBOOST_CATEGORY
        )
    );

    private static ClientPacketListener activeConnection;
    private static boolean setupShownForConnection;

    @Override
    public void onInitializeClient() {
        NoHurtCamConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPacketListener connection = client.getConnection();
            if (client.player == null || client.level == null || connection == null) {
                activeConnection = null;
                setupShownForConnection = false;
                return;
            }

            if (connection != activeConnection) {
                activeConnection = connection;
                setupShownForConnection = false;
            }

            while (TOGGLE_NO_HURT_CAM.consumeClick()) {
                if (client.screen == null) {
                    boolean enabled = NoHurtCamConfig.toggleEnabled();
                    client.player.displayClientMessage(
                        Component.translatable(enabled
                            ? "message.vulkanboost.no_hurt_cam.enabled"
                            : "message.vulkanboost.no_hurt_cam.disabled"),
                        true
                    );
                }
            }

            while (OPEN_NO_HURT_CAM_SETTINGS.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new NoHurtCamScreen(null));
                }
            }

            if (!client.isSingleplayer()
                && !NoHurtCamConfig.isSetupComplete()
                && !setupShownForConnection
                && client.screen == null) {
                setupShownForConnection = true;
                client.setScreen(new NoHurtCamSetupScreen());
            }
        });
    }

    public static InputConstants.Key getToggleKey() {
        return TOGGLE_NO_HURT_CAM.getKey();
    }

    public static InputConstants.Key getOpenSettingsKey() {
        return OPEN_NO_HURT_CAM_SETTINGS.getKey();
    }

    public static Component getToggleKeyName() {
        return TOGGLE_NO_HURT_CAM.getTranslatedKeyMessage();
    }

    public static boolean isToggleKeyAssigned() {
        return TOGGLE_NO_HURT_CAM.getKey().getValue() != GLFW.GLFW_KEY_UNKNOWN;
    }

    public static void setToggleKeyInMemory(InputConstants.Key key) {
        TOGGLE_NO_HURT_CAM.setKey(key);
        KeyMapping.resetMapping();
    }

    public static void saveKeyMappings() {
        Minecraft.getInstance().options.save();
    }
}
