package org.yuemi.environmentmanager.plugin;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.yuemi.environmentmanager.api.ExampleApi;

import java.util.UUID;

final class BungeeApiImpl implements ExampleApi {

    private final ProxyServer proxy;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    BungeeApiImpl(ProxyServer proxy) {
        this.proxy = proxy;
    }

    @Override
    public void sendMessage(
            @NotNull UUID playerUuid,
            @NotNull String message
    ) {
        if (proxy.getPlayer(playerUuid) != null) {
            proxy.getPlayer(playerUuid).sendMessage(
                BungeeComponentSerializer.get().serialize(miniMessage.deserialize(message))
            );
        }
    }

    @Override
    public boolean isFeatureEnabled(@NotNull UUID playerUuid) {
        return proxy.getPlayer(playerUuid) != null && proxy.getPlayer(playerUuid).hasPermission("example.feature");
    }
}
