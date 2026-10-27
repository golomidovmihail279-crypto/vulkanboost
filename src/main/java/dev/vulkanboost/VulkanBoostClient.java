package dev.vulkanboost;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class VulkanBoostClient implements ClientModInitializer {
    private static final KeyMapping.Category VULKANBOOST_CATEGORY =
        KeyMapping.Category.register(Identifier.fromNamespaceAndPath("vulkanboost", "controls"));

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
        // fabric.mod.json depends on BetterHurtCam, so its config manager and real
        // keybindings are initialized before this compatibility UI starts.
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

    public static InputConstants.Key getOpenSettingsKey() {
        return InputConstants.getKey(OPEN_NO_HURT_CAM_SETTINGS.saveString());
    }

    public static Component getOpenSettingsKeyName() {
        return OPEN_NO_HURT_CAM_SETTINGS.getTranslatedKeyMessage();
    }

    public static boolean isOpenSettingsKeyAssigned() {
        return !OPEN_NO_HURT_CAM_SETTINGS.isUnbound();
    }

    public static void setOpenSettingsKeyInMemory(InputConstants.Key key) {
        OPEN_NO_HURT_CAM_SETTINGS.setKey(key);
        KeyMapping.resetMapping();
    }

    /** Checks the selected settings key against every other vanilla or mod key mapping. */
    public static boolean hasKeyConflict(InputConstants.Key candidate) {
        for (KeyMapping mapping : Minecraft.getInstance().options.keyMappings) {
            if (mapping != OPEN_NO_HURT_CAM_SETTINGS
                && InputConstants.getKey(mapping.saveString()).equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    public static void saveKeyMappings() {
        Minecraft.getInstance().options.save();
    }
}
