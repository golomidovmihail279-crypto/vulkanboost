package dev.vulkanboost;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class NoHurtCamConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanBoost");
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir().resolve("vulkanboost-no-hurt-cam.properties");
    private static volatile int reduction = 100;

    private NoHurtCamConfig() {}

    public static int getReduction() {
        return reduction;
    }

    public static void setReductionInMemory(int value) {
        reduction = Math.max(0, Math.min(100, value));
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            reduction = 100;
            return;
        }
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
            properties.load(input);
            setReductionInMemory(Integer.parseInt(properties.getProperty("hurtCamReduction", "100").trim()));
        } catch (IOException | NumberFormatException exception) {
            LOGGER.error("Could not load VulkanBoost NoHurtCam settings; using defaults.", exception);
            reduction = 100;
        }
    }

    public static void save() {
        Properties properties = new Properties();
        properties.setProperty("hurtCamReduction", Integer.toString(reduction));
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(output, "0 = vanilla camera shake; 100 = no hurt-camera tilt.");
            }
        } catch (IOException exception) {
            LOGGER.error("Could not save VulkanBoost NoHurtCam settings.", exception);
        }
    }
}
