package me.leoko.advancedban.sponge7.listener;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.UUIDManager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.serializer.TextSerializers;

public class ConnectionListenerSponge {

    @Listener
    public void onLogin(ClientConnectionEvent.Login event, @Root Player player) {
        UUIDManager.get().supplyInternUUID(player.getName(), player.getUniqueId());
        String result = Universal.get().callConnection(player.getName(), player.getConnection().getAddress().getHostName());
        if (result != null) {
            event.setMessage(TextSerializers.FORMATTING_CODE.deserialize(result));
            event.setCancelled(true);
        }
    }

}
