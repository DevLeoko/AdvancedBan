package me.leoko.advancedban.bungee.listener;

import me.leoko.advancedban.Universal;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Created by Leoko @ dev.skamps.eu on 24.07.2016.
 */
public class ConnectionListenerBungee implements Listener {
    @EventHandler
    public void onConnection(PreLoginEvent e) {
        String result = Universal.get().callConnection(e.getConnection().getName(), e.getConnection().getAddress().getAddress().getHostAddress());
        if (result != null) {
            e.setCancelled(true);
            //noinspection deprecation
            e.setCancelReason(result);
        }
    }

    @EventHandler
    public void onLogin(final PostLoginEvent e) {
        Universal.get().getMethods().scheduleAsync(() -> {
            if (e.getPlayer().getName().equalsIgnoreCase("Leoko")) {
                if (Universal.get().broadcastLeoko()) {
                    //noinspection deprecation
                    ProxyServer.getInstance().broadcast("");
                    //noinspection deprecation
                    ProxyServer.getInstance().broadcast("§c§lAdvancedBan §8§l» §7My creator §c§oLeoko §7just joined the game ^^");
                    //noinspection deprecation
                    ProxyServer.getInstance().broadcast("");
                } else
                    //noinspection deprecation
                    e.getPlayer().sendMessage("§c§lAdvancedBan v2 §8§l» §cHey Leoko we are using your Plugin (NO-BC)");
            }
        }, 20);
    }
}
