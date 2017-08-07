package me.leoko.advancedban.bungee.listener;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.PunishmentManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Created by Leoko @ dev.skamps.eu on 24.07.2016.
 */
public class ConnectionListenerBungee implements Listener {
    @EventHandler
    public void onConnection(PreLoginEvent event) {
        String result = Universal.get().callConnection(event.getConnection().getName(), event.getConnection().getAddress().getAddress().getHostAddress());
        if (result != null) {
            event.setCancelled(true);
            event.setCancelReason(result);
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event){
        PunishmentManager.get().discard(event.getPlayer().getName());
    }

    @EventHandler
    public void onLogin(final PostLoginEvent event) {
        Universal.get().getMethods().scheduleAsync(() -> {
            if (event.getPlayer().getName().equalsIgnoreCase("Leoko")) {
                if (Universal.get().broadcastLeoko()) {
                    ProxyServer.getInstance().broadcast("");
                    ProxyServer.getInstance().broadcast("§c§lAdvancedBan §8§l» §7My creator §c§oLeoko §7just joined the game ^^");
                    ProxyServer.getInstance().broadcast("");
                } else {
                    event.getPlayer().sendMessage("§c§lAdvancedBan v2 §8§l» §cHey Leoko we are using your Plugin (NO-BC)");
                }
            }
        }, 20);
    }
}
