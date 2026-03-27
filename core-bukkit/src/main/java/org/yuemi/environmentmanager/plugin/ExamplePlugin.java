package org.yuemi.environmentmanager.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.yuemi.environmentmanager.api.EnvironmentManager;
import org.yuemi.environmentmanager.api.ExampleApi;
import org.yuemi.environmentmanager.api.config.ConfigurationManager;

import java.io.File;
import java.nio.file.Path;

public final class ExamplePlugin extends JavaPlugin implements CommandExecutor {

    private ExampleApi api;
    private EnvironmentManager envManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        ConfigurationManager configManager = new ConfigurationManager();
        this.envManager = new EnvironmentManager(configManager, getLogger());

        try {
            envManager.loadConfig(getDataFolder().toPath().resolve("config.yml"));
            envManager.applyEnvironmentKeys(getServer().getWorldContainer().toPath().toAbsolutePath());
        } catch (ConfigurateException e) {
            getLogger().severe("Failed to load environment configuration: " + e.getMessage());
        }

        this.api = new ExampleApiImpl();

        getServer().getServicesManager().register(
                ExampleApi.class,
                api,
                this,
                ServicePriority.Normal
        );

        getCommand("envmanager").setExecutor(this);
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregister(ExampleApi.class, api);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("envmanager.reload")) {
                sender.sendMessage("§cYou don't have permission to do this.");
                return true;
            }

            try {
                envManager.loadConfig(getDataFolder().toPath().resolve("config.yml"));
                envManager.applyEnvironmentKeys(getServer().getWorldContainer().toPath().toAbsolutePath());
                sender.sendMessage("§aEnvironment configuration reloaded and applied!");
            } catch (ConfigurateException e) {
                sender.sendMessage("§cFailed to reload configuration: " + e.getMessage());
            }
            return true;
        }
        return false;
    }
}
