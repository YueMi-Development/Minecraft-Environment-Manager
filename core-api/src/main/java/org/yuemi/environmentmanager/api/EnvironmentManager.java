package org.yuemi.environmentmanager.api;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.yuemi.environmentmanager.api.config.ConfigurationManager;
import org.yuemi.environmentmanager.api.config.TextualConfigurationEditor;
import org.yuemi.environmentmanager.api.HijackManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manager class for environment variables and their application to target files.
 */
public final class EnvironmentManager {

    /**
     * The latest supported configuration version.
     */
    public static final int LATEST_CONFIG_VERSION = 1;

    private final ConfigurationManager configManager;
    private final Logger logger;
    private final Map<String, String> environmentKeys = new HashMap<>();
    private ConfigurationNode config;
    private Path basePath;

    /**
     * Constructs a new EnvironmentManager.
     *
     * @param configManager the configuration manager
     * @param logger        the logger
     */
    public EnvironmentManager(@NotNull ConfigurationManager configManager, @NotNull Logger logger) {
        this.configManager = configManager;
        this.logger = logger;
    }

    /**
     * Loads the environment configuration from a path.
     *
     * @param configPath the path to the configuration file
     * @throws ConfigurateException if an error occurs during loading
     */
    public void loadConfig(@NotNull Path configPath) throws ConfigurateException {
        this.basePath = configPath.toAbsolutePath().getParent();
        this.config = configManager.load(configPath);

        int currentVersion = config.node("config-version").getInt();
        if (currentVersion < LATEST_CONFIG_VERSION) {
            migrateConfig(configPath, currentVersion);
        }

        loadEnvironments();
        loadHijackConfig();
    }

    private void loadHijackConfig() {
        boolean hijackEnabled = config.node("hijack", "enabled").getBoolean(false);
        HijackManager.getInstance().setEnabled(hijackEnabled);
        if (hijackEnabled) {
            logger.info("Hijack mode is enabled. Changes will be applied in-memory.");
        }
    }

    /**
     * Reloads the configuration and re-applies environment keys to all targets.
     *
     * @param configPath the path to the configuration file
     * @param rootPath   the root path to resolve relative target paths
     * @throws ConfigurateException if an error occurs during loading
     */
    public void reload(@NotNull Path configPath, @NotNull Path rootPath) throws ConfigurateException {
        logger.info("Reloading environment manager configuration...");
        loadConfig(configPath);
        applyEnvironmentKeys(rootPath);
        logger.info("Environment manager successfully reloaded and applied!");
    }

    private void migrateConfig(Path configPath, int currentVersion) {
        logger.info("Migrating configuration from version " + currentVersion + " to " + LATEST_CONFIG_VERSION);
        try {
            String content = Files.readString(configPath);
            content = TextualConfigurationEditor.update(content, "config-version", String.valueOf(LATEST_CONFIG_VERSION));
            Files.writeString(configPath, content);
        } catch (IOException e) {
            logger.severe("Failed to save migrated configuration: " + e.getMessage());
        }
    }

    private void loadEnvironments() {
        environmentKeys.clear();
        ConfigurationNode envNode = config.node("environments");
        if (!envNode.virtual()) {
            envNode.childrenMap().forEach((keyObj, node) -> {
                String key = keyObj.toString();
                String value = node.getString();

                if (key.startsWith("@")) {
                    // Bulk load from .env
                    if (value != null) {
                        String actualPath = value.startsWith("@") ? value.substring(1) : value;
                        loadFromEnvFile(basePath.resolve(actualPath));
                    }
                } else if (value != null && value.startsWith("@")) {
                    String subPath = value.substring(1);
                    Path filePath = basePath.resolve(subPath);
                    if (subPath.toLowerCase().endsWith(".env")) {
                        // Extract specific key from .env file
                        String envVal = getValFromEnvFile(filePath, key);
                        if (envVal != null) {
                            environmentKeys.put(key, envVal);
                        } else {
                            logger.warning("Key '" + key + "' not found in .env file: " + filePath);
                        }
                    } else {
                        // Load whole file content as value
                        try {
                            environmentKeys.put(key, Files.readString(filePath).trim());
                        } catch (IOException e) {
                            logger.severe("Failed to load environment key '" + key + "' from file: " + filePath + " - " + e.getMessage());
                        }
                    }
                } else if (value != null) {
                    environmentKeys.put(key, value);
                }
            });
        }
        logger.info("Loaded " + environmentKeys.size() + " environment keys.");
    }

    private void loadFromEnvFile(Path filePath) {
        Path absPath = filePath.toAbsolutePath().normalize();
        try {
            List<String> lines = Files.readAllLines(absPath);
            for (String line : lines) {
                parseAndPutEnvLine(line);
            }
        } catch (IOException e) {
            logger.severe("Failed to load .env file: " + absPath + " - " + e.getMessage());
        }
    }

    private String getValFromEnvFile(Path filePath, String targetKey) {
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eqIndex = line.indexOf('=');
                if (eqIndex > 0) {
                    String key = line.substring(0, eqIndex).trim();
                    if (key.equals(targetKey)) {
                        return cleanEnvValue(line.substring(eqIndex + 1).trim());
                    }
                }
            }
        } catch (IOException e) {
            logger.severe("Failed to read .env file for key '" + targetKey + "': " + filePath + " - " + e.getMessage());
        }
        return null;
    }

    private void parseAndPutEnvLine(String line) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) return;
        int eqIndex = line.indexOf('=');
        if (eqIndex > 0) {
            String key = line.substring(0, eqIndex).trim();
            String value = cleanEnvValue(line.substring(eqIndex + 1).trim());
            environmentKeys.put(key, value);
        }
    }

    private String cleanEnvValue(String value) {
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            return value.substring(1, value.length() - 1);
        } else if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    /**
     * Applies the environment keys to the target files.
     *
     * @param rootPath the root path to resolve relative target paths
     */
    public void applyEnvironmentKeys(@NotNull Path rootPath) {
        logger.info("Applying environment mappings to target files...");
        ConfigurationNode targetsNode = config.node("targets");
        if (targetsNode.virtual()) return;

        targetsNode.childrenList().forEach(target -> {
            String relativePath = target.node("path").getString();
            if (relativePath == null) return;

            Path targetPath = rootPath.resolve(relativePath);
            Map<Object, ? extends ConfigurationNode> mappings = target.node("mappings").childrenMap();

            try {
                String content = Files.readString(targetPath);
                boolean changed = false;

                for (Map.Entry<Object, ? extends ConfigurationNode> entry : mappings.entrySet()) {
                    String targetKey = entry.getKey().toString();
                    String envKey = entry.getValue().getString();

                    if (envKey != null && environmentKeys.containsKey(envKey)) {
                        String value = environmentKeys.get(envKey);
                        content = TextualConfigurationEditor.update(content, targetKey, value);
                        changed = true;
                    }
                }

                if (changed) {
                    if (HijackManager.getInstance().isEnabled()) {
                        HijackManager.getInstance().register(targetPath, content.getBytes(StandardCharsets.UTF_8));
                        logger.info("Hijacked (in-memory): " + relativePath);
                    } else {
                        Files.writeString(targetPath, content);
                        logger.info("Applied environment keys to: " + relativePath);
                    }
                }

            } catch (IOException e) {
                logger.severe("Failed to apply environment keys to " + relativePath + ": " + e.getMessage());
            }
        });
    }


    /**
     * Gets the loaded environment keys.
     *
     * @return a map of environment keys and their values
     */
    public Map<String, String> getEnvironmentKeys() {
        return new HashMap<>(environmentKeys);
    }
}
