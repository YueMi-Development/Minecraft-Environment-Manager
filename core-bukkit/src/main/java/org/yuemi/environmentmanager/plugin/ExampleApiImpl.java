package org.yuemi.environmentmanager.plugin;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.yuemi.environmentmanager.api.ExampleApi;

import java.util.UUID;

final class ExampleApiImpl implements ExampleApi {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void sendMessage(
            @NotNull UUID playerUuid,
            @NotNull String message
    ) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null) {
            player.sendMessage(miniMessage.deserialize(message));
        }
    }

    @Override
    public boolean isFeatureEnabled(@NotNull UUID playerUuid) {
        Player player = Bukkit.getPlayer(playerUuid);
        return player != null && player.hasPermission("example.feature");
    }
}
