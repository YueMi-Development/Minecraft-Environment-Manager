package org.yuemi.environmentmanager.api;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface ExampleApi {

    void sendMessage(
            @NotNull UUID playerUuid,
            @NotNull String message
    );

    boolean isFeatureEnabled(@NotNull UUID playerUuid);
}
