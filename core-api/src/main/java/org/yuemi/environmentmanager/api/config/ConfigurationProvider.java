package org.yuemi.environmentmanager.api.config;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;
import java.nio.file.Path;

/**
 * Interface for configuration providers that can load and save configuration nodes.
 */
public interface ConfigurationProvider {

    /**
     * Loads a configuration node from a path.
     *
     * @param path the path to load from
     * @return the loaded configuration node
     * @throws ConfigurateException if an error occurs during loading
     */
    @NotNull
    ConfigurationNode load(@NotNull Path path) throws ConfigurateException;

    /**
     * Saves a configuration node to a path.
     *
     * @param path the path to save to
     * @param node the configuration node to save
     * @throws ConfigurateException if an error occurs during saving
     */
    void save(@NotNull Path path, @NotNull ConfigurationNode node) throws ConfigurateException;

    /**
     * Loads a configuration node from a file.
     *
     * @param file the file to load from
     * @return the loaded configuration node
     * @throws ConfigurateException if an error occurs during loading
     */
    default @NotNull ConfigurationNode load(@NotNull File file) throws ConfigurateException {
        return load(file.toPath());
    }

    /**
     * Saves a configuration node to a file.
     *
     * @param file the file to save to
     * @param node the configuration node to save
     * @throws ConfigurateException if an error occurs during saving
     */
    default void save(@NotNull File file, @NotNull ConfigurationNode node) throws ConfigurateException {
        save(file.toPath(), node);
    }
}
