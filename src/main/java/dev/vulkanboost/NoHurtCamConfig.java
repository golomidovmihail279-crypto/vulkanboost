package dev.vulkanboost;

import net.fabricmc.loader.api.FabricLoader;
import net.uku3lig.betterhurtcam.BetterHurtCam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Compatibility facade for VulkanBoost's setup/settings screens.
 * BetterHurtCam owns the real hurt-camera behavior and its multiplier config.
 */
public final class NoHurtCamConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanBoost");
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir().resolve("vulkanboost-no-hurt-cam.properties");
    private static final Path BETTER_HURT_CAM_CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir().resolve("betterhurtcam.toml");

    private static volatile boolean setupComplete;

    private NoHurtCamConfig() {}

    /** The UI expresses reduction, while BetterHurtCam stores the remaining multiplier. */
    public static int getReduction() {
        double multiplier = BetterHurtCam.getManager().getConfig().getMultiplier();
        int reduction = (int) Math.round((1.0 - multiplier) * 100.0);
        return Math.max(0, Math.min(100, reduction));
    }

    public static boolean isEnabled() {
        return BetterHurtCam.getManager().getConfig().isEnabled();
    }

    public static boolean isSetupComplete() {
        return setupComplete;
    }

    public static void setReductionInMemory(int value) {
        int reduction = Math.max(0, Math.min(100, value));
        BetterHurtCam.getManager().getConfig().setMultiplier(1.0 - (reduction / 100.0));
    }

    public static void setEnabledInMemory(boolean value) {
        BetterHurtCam.getManager().getConfig().setEnabled(value);
    }

    public static boolean toggleEnabled() {
        setEnabledInMemory(!isEnabled());
        save();
        return isEnabled();
    }

    public static void setSetupCompleteInMemory(boolean value) {
        setupComplete = value;
    }

    /**
     * Imports older VulkanBoost settings once. BetterHurtCam's TOML is the source
     * of truth afterwards; an existing upstream config is preserved unless there
     * are actual old VulkanBoost values to migrate.
     */
    public static void load() {
        boolean upstreamConfigExists = Files.exists(BETTER_HURT_CAM_CONFIG_PATH);
        boolean migrationComplete = false;

        if (Files.exists(CONFIG_PATH)) {
            Properties properties = new Properties();
            try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
                properties.load(input);
                setupComplete = Boolean.parseBoolean(
                    properties.getProperty("setupComplete", "false").trim()
                );
                migrationComplete = Boolean.parseBoolean(
                    properties.getProperty("betterHurtCamMigrationComplete", "false").trim()
                );

                if (!migrationComplete) {
                    boolean hasLegacySettings =
                        properties.containsKey("hurtCamReduction") || properties.containsKey("enabled");
                    if (hasLegacySettings) {
                        int legacyReduction = Integer.parseInt(
                            properties.getProperty("hurtCamReduction", "100").trim()
                        );
                        boolean legacyEnabled = Boolean.parseBoolean(
                            properties.getProperty("enabled", "true").trim()
                        );
                        setReductionInMemory(legacyReduction);
                        setEnabledInMemory(legacyEnabled);
                        BetterHurtCam.getManager().saveConfig();
                    } else if (!upstreamConfigExists) {
                        setReductionInMemory(100);
                        setEnabledInMemory(true);
                        BetterHurtCam.getManager().saveConfig();
                    }
                    migrationComplete = true;
                }
            } catch (IOException | NumberFormatException exception) {
                LOGGER.error("Could not migrate old VulkanBoost NoHurtCam settings.", exception);
                setupComplete = false;
                if (!upstreamConfigExists) {
                    setReductionInMemory(100);
                    setEnabledInMemory(true);
                    BetterHurtCam.getManager().saveConfig();
                }
                migrationComplete = upstreamConfigExists;
            }
        } else {
            setupComplete = false;
            if (!upstreamConfigExists) {
                // Preserve the previous VulkanBoost default: completely remove hurt tilt.
                setReductionInMemory(100);
                setEnabledInMemory(true);
                BetterHurtCam.getManager().saveConfig();
            }
            migrationComplete = true;
        }

        saveSetupStateOnly(migrationComplete);
    }

    public static void save() {
        BetterHurtCam.getManager().saveConfig();
        saveSetupStateOnly(true);
    }

    private static void saveSetupStateOnly(boolean migrationComplete) {
        Properties properties = new Properties();
        properties.setProperty("setupComplete", Boolean.toString(setupComplete));
        properties.setProperty("betterHurtCamMigrationComplete", Boolean.toString(migrationComplete));

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(
                    output,
                    "VulkanBoost setup state. Hurt-camera settings are stored by BetterHurtCam in betterhurtcam.toml."
                );
            }
        } catch (IOException exception) {
            LOGGER.error("Could not save VulkanBoost setup state.", exception);
        }
    }
}
