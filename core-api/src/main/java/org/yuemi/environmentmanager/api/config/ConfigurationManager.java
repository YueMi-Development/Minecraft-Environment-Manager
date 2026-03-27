package org.yuemi.environmentmanager.api.config;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ConfigurationManager {

    private final Map<String, ConfigurationProvider> providers = new HashMap<>();

    public ConfigurationManager() {
        registerDefaultProviders();
    }

    private void registerDefaultProviders() {
        registerProvider("yml", new YamlProvider());
        registerProvider("yaml", new YamlProvider());
        registerProvider("json", new JsonProvider());
        registerProvider("conf", new HoconProvider());
    }

    public void registerProvider(@NotNull String extension, @NotNull ConfigurationProvider provider) {
        providers.put(extension.toLowerCase(), provider);
    }

    @NotNull
    public ConfigurationNode load(@NotNull Path path) throws ConfigurateException {
        String extension = getExtension(path);
        ConfigurationProvider provider = providers.get(extension.toLowerCase());

        if (provider == null) {
            throw new ConfigurateException("No provider registered for extension: " + extension);
        }

        return provider.load(path);
    }

    public void save(@NotNull Path path, @NotNull ConfigurationNode node) throws ConfigurateException {
        String extension = getExtension(path);
        ConfigurationProvider provider = providers.get(extension.toLowerCase());

        if (provider == null) {
            throw new ConfigurateException("No provider registered for extension: " + extension);
        }

        provider.save(path, node);
    }

    private String getExtension(Path path) {
        String filename = path.getFileName().toString();
        int lastIndex = filename.lastIndexOf('.');
        return (lastIndex == -1) ? "" : filename.substring(lastIndex + 1);
    }

    private static final class YamlProvider implements ConfigurationProvider {
        @Override
        public @NotNull ConfigurationNode load(@NotNull Path path) throws ConfigurateException {
            return YamlConfigurationLoader.builder().path(path).build().load();
        }

        @Override
        public void save(@NotNull Path path, @NotNull ConfigurationNode node) throws ConfigurateException {
            YamlConfigurationLoader.builder().path(path).build().save(node);
        }
    }

    private static final class JsonProvider implements ConfigurationProvider {
        @Override
        public @NotNull ConfigurationNode load(@NotNull Path path) throws ConfigurateException {
            return GsonConfigurationLoader.builder().path(path).build().load();
        }

        @Override
        public void save(@NotNull Path path, @NotNull ConfigurationNode node) throws ConfigurateException {
            GsonConfigurationLoader.builder().path(path).build().save(node);
        }
    }

    private static final class HoconProvider implements ConfigurationProvider {
        @Override
        public @NotNull ConfigurationNode load(@NotNull Path path) throws ConfigurateException {
            return HoconConfigurationLoader.builder().path(path).build().load();
        }

        @Override
        public void save(@NotNull Path path, @NotNull ConfigurationNode node) throws ConfigurateException {
            HoconConfigurationLoader.builder().path(path).build().save(node);
        }
    }
}
