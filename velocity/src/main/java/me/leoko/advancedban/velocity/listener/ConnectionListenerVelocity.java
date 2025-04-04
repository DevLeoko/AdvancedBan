package me.leoko.advancedban.velocity.listener;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ConnectionListenerVelocity {

    private final ProxyServer server;

    public ConnectionListenerVelocity(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();
        UUIDManager.get().supplyInternUUID(player.getUsername(), player.getUniqueId());
        String result = Universal.get().callConnection(player.getUsername(), player.getRemoteAddress().getAddress().getHostAddress());
        if (result != null) {
            event.setResult(ResultedEvent.ComponentResult.denied(LegacyComponentSerializer.legacyAmpersand().deserialize(result)));
        }
    }

    @Subscribe
    public void onQuit(DisconnectEvent event) {
        Universal.get().getMethods().runAsync(() -> {
            if (event.getPlayer() != null) {
                PunishmentManager.get().discard(event.getPlayer().getUsername());
            }
        });
    }

    @Subscribe
    public void onLeokoLogin(PostLoginEvent event) {
        Universal.get().getMethods().scheduleAsync(() -> {
            if (event.getPlayer().getUsername().equalsIgnoreCase("Leoko")) {
                event.getPlayer().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("§c§lAdvancedBan v2 §8§l» §cHey Leoko we are using your Plugin (NO-BC)"));
            }
        }, 20);
    }

}
