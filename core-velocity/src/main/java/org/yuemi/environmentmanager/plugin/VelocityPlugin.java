package org.yuemi.environmentmanager.plugin;

import com.google.inject.Inject;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurateException;
import org.yuemi.environmentmanager.api.EnvironmentManager;
import org.yuemi.environmentmanager.api.config.ConfigurationManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(
        id = "environment-manager",
        name = "EnvironmentManager",
        version = "@version@",
        authors = {"YueMi-Development", "NekoMonci12"}
)
public final class VelocityPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private EnvironmentManager envManager;

    @Inject
    public VelocityPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        // Load and apply environment keys as early as possible (in constructor)
        saveDefaultConfig();
        ConfigurationManager configManager = new ConfigurationManager();
        this.envManager = new EnvironmentManager(configManager, java.util.logging.Logger.getLogger("EnvironmentManager"));

        try {
            envManager.loadConfig(dataDirectory.resolve("config.yml"));
            envManager.applyEnvironmentKeys(Path.of("").toAbsolutePath());
        } catch (ConfigurateException e) {
            logger.error("Failed to load environment configuration: " + e.getMessage());
        }
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("EnvironmentManager (Velocity) has been enabled!");
        server.getCommandManager().register("envmanager", new EnvCommand());
    }

    private void saveDefaultConfig() {
        if (Files.exists(dataDirectory.resolve("config.yml"))) {
            return;
        }

        if (!Files.exists(dataDirectory)) {
            try {
                Files.createDirectories(dataDirectory);
            } catch (IOException e) {
                logger.error("Failed to create data directory: " + e.getMessage());
                return;
            }
        }

        try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
            if (in == null) {
                logger.error("Failed to find default config.yml in resources.");
                return;
            }
            Files.copy(in, dataDirectory.resolve("config.yml"));
            logger.info("Generated default config.yml");
        } catch (IOException e) {
            logger.error("Failed to save default config: " + e.getMessage());
        }
    }

    private final class EnvCommand implements SimpleCommand {
        @Override
        public void execute(Invocation invocation) {
            String[] args = invocation.arguments();
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!invocation.source().hasPermission("envmanager.reload")) {
                    invocation.source().sendMessage(Component.text("You don't have permission to do this.", NamedTextColor.RED));
                    return;
                }

                try {
                    envManager.reload(dataDirectory.resolve("config.yml"), Path.of("").toAbsolutePath());
                    invocation.source().sendMessage(Component.text("Environment configuration reloaded and applied!", NamedTextColor.GREEN));
                } catch (ConfigurateException e) {
                    invocation.source().sendMessage(Component.text("Failed to reload configuration: " + e.getMessage(), NamedTextColor.RED));
                }
            }
        }
    }
}
