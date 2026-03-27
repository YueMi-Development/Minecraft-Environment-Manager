package org.yuemi.environmentmanager.plugin;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import org.yuemi.environmentmanager.api.ExampleApi;

@Plugin(
        id = "example-plugin",
        name = "ExamplePlugin",
        version = "1.0.0-SNAPSHOT",
        authors = {"YueMi-Development"}
)
public final class VelocityPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private ExampleApi api;

    @Inject
    public VelocityPlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.api = new VelocityApiImpl(server);
        logger.info("ExamplePlugin (Velocity) has been enabled!");
    }
}
