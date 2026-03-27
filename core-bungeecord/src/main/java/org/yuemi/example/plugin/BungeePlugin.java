package org.yuemi.example.plugin;

import net.md_5.bungee.api.plugin.Plugin;
import org.yuemi.example.api.ExampleApi;

public final class BungeePlugin extends Plugin {

    private ExampleApi api;

    @Override
    public void onEnable() {
        this.api = new BungeeApiImpl(getProxy());
        getLogger().info("ExamplePlugin (BungeeCord) has been enabled!");
    }
}
