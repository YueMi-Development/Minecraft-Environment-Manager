package org.yuemi.environmentmanager.plugin;

import net.md_5.bungee.api.plugin.Plugin;
import org.yuemi.environmentmanager.api.ExampleApi;

public final class BungeePlugin extends Plugin {

    private ExampleApi api;

    @Override
    public void onEnable() {
        this.api = new BungeeApiImpl(getProxy());
        getLogger().info("ExamplePlugin (BungeeCord) has been enabled!");
    }
}
