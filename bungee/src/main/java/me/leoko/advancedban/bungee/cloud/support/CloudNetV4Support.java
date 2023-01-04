package me.leoko.advancedban.bungee.cloud.support;

import eu.cloudnetservice.driver.CloudNetDriver;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import me.leoko.advancedban.bungee.cloud.CloudSupport;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Objects;
import java.util.UUID;

public class CloudNetV4Support implements CloudSupport {
    @Override
    public void kick(UUID uniqueID, String reason) {
        Objects.requireNonNull(CloudNetDriver.instance().serviceRegistry()
                        .firstProvider(PlayerManager.class)
                        .onlinePlayer(uniqueID),"player is null in CloudNetV4")
                .playerExecutor()
                .kick(LegacyComponentSerializer.legacySection().deserialize(reason));
    }
}
