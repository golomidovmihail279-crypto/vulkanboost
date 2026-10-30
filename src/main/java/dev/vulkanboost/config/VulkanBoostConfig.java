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
    private static final String MEMORY_ASSIST_KEY = "memory.autoReduceDistances";
    private static final String MEMORY_THRESHOLD_KEY = "memory.pressureThresholdPercent";
    private static final Path CONFIG_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("vulkanboost.properties");

    private static boolean showHints = true;
    private static boolean memoryAssistEnabled;
    private static int memoryThresholdPercent = 85;
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
        String memoryAssistValue = properties.getProperty(MEMORY_ASSIST_KEY, "false");
        String memoryThresholdValue = properties.getProperty(MEMORY_THRESHOLD_KEY, "85");

        if (!isBoolean(showHintsValue) || !isBoolean(memoryAssistValue)) {
            readOnly = true;
            LOGGER.warn("Invalid boolean in VulkanBoost config. Defaults will be used and the file will be left untouched.");
            return;
        }

        final int parsedThreshold;
        try {
            parsedThreshold = Integer.parseInt(memoryThresholdValue);
        } catch (NumberFormatException exception) {
            readOnly = true;
            LOGGER.warn("Invalid memory threshold '{}'. Defaults will be used and the file will be left untouched.",
                    memoryThresholdValue);
            return;
        }

        if (parsedThreshold < 70 || parsedThreshold > 95) {
            readOnly = true;
            LOGGER.warn("Memory threshold must be between 70 and 95. The config file will be left untouched.");
            return;
        }

        showHints = Boolean.parseBoolean(showHintsValue);
        memoryAssistEnabled = Boolean.parseBoolean(memoryAssistValue);
        memoryThresholdPercent = parsedThreshold;
    }

    private static boolean isBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }

    public static boolean showHints() {
        return showHints;
    }

    public static void setShowHints(boolean enabled) {
        showHints = enabled;
        save();
    }

    public static boolean memoryAssistEnabled() {
        return memoryAssistEnabled;
    }

    public static void setMemoryAssistEnabled(boolean enabled) {
        memoryAssistEnabled = enabled;
        save();
    }

    public static int memoryThresholdPercent() {
        return memoryThresholdPercent;
    }

    public static void setMemoryThresholdPercent(int threshold) {
        if (threshold < 70 || threshold > 95) {
            throw new IllegalArgumentException("Memory pressure threshold must be between 70 and 95.");
        }
        memoryThresholdPercent = threshold;
        save();
    }

    public static void resetToDefaults() {
        showHints = true;
        memoryAssistEnabled = false;
        memoryThresholdPercent = 85;
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
        properties.setProperty(MEMORY_ASSIST_KEY, Boolean.toString(memoryAssistEnabled));
        properties.setProperty(MEMORY_THRESHOLD_KEY, Integer.toString(memoryThresholdPercent));

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
