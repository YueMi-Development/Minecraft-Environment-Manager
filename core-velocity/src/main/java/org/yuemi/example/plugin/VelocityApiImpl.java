package org.yuemi.example.plugin;

import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.yuemi.example.api.ExampleApi;

import java.util.UUID;

final class VelocityApiImpl implements ExampleApi {

    private final ProxyServer server;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    VelocityApiImpl(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void sendMessage(
            @NotNull UUID playerUuid,
            @NotNull String message
    ) {
        server.getPlayer(playerUuid).ifPresent(player -> 
            player.sendMessage(miniMessage.deserialize(message))
        );
    }

    @Override
    public boolean isFeatureEnabled(@NotNull UUID playerUuid) {
        return server.getPlayer(playerUuid)
                .map(player -> player.hasPermission("example.feature"))
                .orElse(false);
    }
}
