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
import org.yuemi.environmentmanager.api.ExampleApi;
import org.yuemi.environmentmanager.api.config.ConfigurationManager;

import java.nio.file.Path;

@Plugin(
        id = "environment-manager",
        name = "EnvironmentManager",
        version = "1.0.0-SNAPSHOT",
        authors = {"YueMi-Development"}
)
public final class VelocityPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private ExampleApi api;
    private EnvironmentManager envManager;

    @Inject
    public VelocityPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        ConfigurationManager configManager = new ConfigurationManager();
        this.envManager = new EnvironmentManager(configManager, java.util.logging.Logger.getLogger("EnvironmentManager"));

        try {
            envManager.loadConfig(dataDirectory.resolve("config.yml"));
            envManager.applyEnvironmentKeys(Path.of("").toAbsolutePath());
        } catch (ConfigurateException e) {
            logger.error("Failed to load environment configuration: " + e.getMessage());
        }

        this.api = new VelocityApiImpl(server);
        logger.info("EnvironmentManager (Velocity) has been enabled!");

        server.getCommandManager().register("envmanager", new EnvCommand());
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
                    envManager.loadConfig(dataDirectory.resolve("config.yml"));
                    envManager.applyEnvironmentKeys(Path.of("").toAbsolutePath());
                    invocation.source().sendMessage(Component.text("Environment configuration reloaded and applied!", NamedTextColor.GREEN));
                } catch (ConfigurateException e) {
                    invocation.source().sendMessage(Component.text("Failed to reload configuration: " + e.getMessage(), NamedTextColor.RED));
                }
            }
        }
    }
}
