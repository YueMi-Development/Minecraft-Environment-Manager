package org.yuemi.environmentmanager.api;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.yuemi.environmentmanager.api.config.ConfigurationManager;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class EnvironmentManager {

    private final ConfigurationManager configManager;
    private final Logger logger;
    private final Map<String, String> environmentKeys = new HashMap<>();
    private ConfigurationNode config;

    public EnvironmentManager(@NotNull ConfigurationManager configManager, @NotNull Logger logger) {
        this.configManager = configManager;
        this.logger = logger;
    }

    public void loadConfig(@NotNull Path configPath) throws ConfigurateException {
        this.config = configManager.load(configPath);
        loadEnvironments();
    }

    private void loadEnvironments() {
        environmentKeys.clear();
        ConfigurationNode envNode = config.node("environments");
        if (!envNode.virtual()) {
            envNode.childrenMap().forEach((key, node) -> {
                environmentKeys.put(key.toString(), node.getString());
            });
        }
        logger.info("Loaded " + environmentKeys.size() + " environment keys.");
    }

    public void applyEnvironmentKeys(@NotNull Path rootPath) {
        ConfigurationNode targetsNode = config.node("targets");
        if (targetsNode.virtual()) return;

        targetsNode.childrenList().forEach(target -> {
            String relativePath = target.node("path").getString();
            if (relativePath == null) return;

            Path targetPath = rootPath.resolve(relativePath);
            Map<Object, ? extends ConfigurationNode> mappings = target.node("mappings").childrenMap();

            try {
                ConfigurationNode targetConfig = configManager.load(targetPath);
                boolean changed = false;

                for (Map.Entry<Object, ? extends ConfigurationNode> entry : mappings.entrySet()) {
                    String targetKey = entry.getKey().toString();
                    String envKey = entry.getValue().getString();

                    if (envKey != null && environmentKeys.containsKey(envKey)) {
                        String value = environmentKeys.get(envKey);
                        String[] path = targetKey.split("\\.");
                        targetConfig.node((Object[]) path).set(value);
                        changed = true;
                    }
                }

                if (changed) {
                    configManager.save(targetPath, targetConfig);
                    logger.info("Applied environment keys to: " + relativePath);
                }

            } catch (ConfigurateException e) {
                logger.severe("Failed to apply environment keys to " + relativePath + ": " + e.getMessage());
            }
        });
    }

    public Map<String, String> getEnvironmentKeys() {
        return new HashMap<>(environmentKeys);
    }
}
