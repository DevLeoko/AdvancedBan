package me.leoko.advancedban.velocity.listener;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.UUIDManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ConnectionListenerVelocity {

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();
        UUIDManager.get().supplyInternUUID(player.getUsername(), player.getUniqueId());
        String result = Universal.get().callConnection(player.getUsername(), player.getRemoteAddress().getAddress().getHostAddress());
        if (result != null) {
            event.setResult(ResultedEvent.ComponentResult.denied(LegacyComponentSerializer.legacyAmpersand().deserialize(result)));
        }
    }

}
