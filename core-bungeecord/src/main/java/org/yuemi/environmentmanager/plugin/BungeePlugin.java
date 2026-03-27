package org.yuemi.environmentmanager.plugin;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import org.spongepowered.configurate.ConfigurateException;
import org.yuemi.environmentmanager.api.EnvironmentManager;
import org.yuemi.environmentmanager.api.config.ConfigurationManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BungeePlugin extends Plugin {

    private EnvironmentManager envManager;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                getLogger().severe("Failed to save default config: " + e.getMessage());
            }
        }

        ConfigurationManager configManager = new ConfigurationManager();
        this.envManager = new EnvironmentManager(configManager, getLogger());

        try {
            envManager.loadConfig(configFile.toPath());
            envManager.applyEnvironmentKeys(Path.of("").toAbsolutePath());
        } catch (ConfigurateException e) {
            getLogger().severe("Failed to load environment configuration: " + e.getMessage());
        }

        getLogger().info("EnvironmentManager (BungeeCord) has been enabled!");

        getProxy().getPluginManager().registerCommand(this, new EnvCommand());
    }

    private final class EnvCommand extends Command {
        public EnvCommand() {
            super("envmanager", "envmanager.command");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("envmanager.reload")) {
                    sender.sendMessage(new TextComponent("§cYou don't have permission to do this."));
                    return;
                }

                try {
                    envManager.reload(new File(getDataFolder(), "config.yml").toPath(), Path.of("").toAbsolutePath());
                    sender.sendMessage(new TextComponent("§aEnvironment configuration reloaded and applied!"));
                } catch (ConfigurateException e) {
                    sender.sendMessage(new TextComponent("§cFailed to reload configuration: " + e.getMessage()));
                }
            }
        }
    }
}
