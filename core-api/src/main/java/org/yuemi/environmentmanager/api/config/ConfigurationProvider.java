package org.yuemi.environmentmanager.api.config;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;
import java.nio.file.Path;

public interface ConfigurationProvider {

    @NotNull
    ConfigurationNode load(@NotNull Path path) throws ConfigurateException;

    void save(@NotNull Path path, @NotNull ConfigurationNode node) throws ConfigurateException;

    default @NotNull ConfigurationNode load(@NotNull File file) throws ConfigurateException {
        return load(file.toPath());
    }

    default void save(@NotNull File file, @NotNull ConfigurationNode node) throws ConfigurateException {
        save(file.toPath(), node);
    }
}
