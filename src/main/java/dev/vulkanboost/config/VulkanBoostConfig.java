package dev.vulkanboost.config;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/**
 * Stores the first-party VulkanBoost settings.
 *
 * The file is deliberately kept separate from third-party mod configuration.
 * A newer or malformed file is preserved rather than silently overwritten.
 */
public final class VulkanBoostConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanBoost");
    private static final int CURRENT_SCHEMA_VERSION = 1;
    private static final String SHOW_HINTS_KEY = "panel.showHints";
    private static final Path CONFIG_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("vulkanboost.properties");

    private static boolean showHints = true;
    private static boolean readOnly;

    private VulkanBoostConfig() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_FILE)) {
            save();
            return;
        }

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException | IllegalArgumentException exception) {
            readOnly = true;
            LOGGER.warn("Could not read {}. Defaults will be used and the file will be left untouched.",
                    CONFIG_FILE, exception);
            return;
        }

        String schemaValue = properties.getProperty("schemaVersion", "1");
        final int schemaVersion;
        try {
            schemaVersion = Integer.parseInt(schemaValue);
        } catch (NumberFormatException exception) {
            readOnly = true;
            LOGGER.warn("Invalid VulkanBoost config schema version '{}'. Keeping the file untouched.", schemaValue);
            return;
        }

        if (schemaVersion != CURRENT_SCHEMA_VERSION) {
            readOnly = true;
            LOGGER.warn("VulkanBoost config schema {} is not supported by this build (expected {}). "
                    + "Defaults will be used and the file will be left untouched.",
                    schemaVersion, CURRENT_SCHEMA_VERSION);
            return;
        }

        String showHintsValue = properties.getProperty(SHOW_HINTS_KEY, "true");
        if (!"true".equalsIgnoreCase(showHintsValue) && !"false".equalsIgnoreCase(showHintsValue)) {
            readOnly = true;
            LOGGER.warn("Invalid value for '{}'. Defaults will be used and the file will be left untouched.",
                    SHOW_HINTS_KEY);
            return;
        }

        showHints = Boolean.parseBoolean(showHintsValue);
    }

    public static boolean showHints() {
        return showHints;
    }

    public static void setShowHints(boolean enabled) {
        showHints = enabled;
        save();
    }

    public static void resetToDefaults() {
        showHints = true;
        save();
    }

    private static void save() {
        if (readOnly) {
            LOGGER.warn("VulkanBoost settings were changed for this session, but the existing config is read-only.");
            return;
        }

        Properties properties = new Properties();
        properties.setProperty("schemaVersion", Integer.toString(CURRENT_SCHEMA_VERSION));
        properties.setProperty(SHOW_HINTS_KEY, Boolean.toString(showHints));

        Path temporaryFile = CONFIG_FILE.resolveSibling(CONFIG_FILE.getFileName() + ".tmp");
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)) {
                properties.store(writer, "VulkanBoost configuration");
            }

            try {
                Files.move(temporaryFile, CONFIG_FILE,
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, CONFIG_FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            LOGGER.error("Could not save VulkanBoost settings to {}.", CONFIG_FILE, exception);
            try {
                Files.deleteIfExists(temporaryFile);
            } catch (IOException cleanupException) {
                LOGGER.warn("Could not remove temporary config file {}.", temporaryFile, cleanupException);
            }
        }
    }
}
