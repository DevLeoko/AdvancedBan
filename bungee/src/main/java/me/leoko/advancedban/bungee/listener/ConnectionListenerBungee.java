package me.leoko.advancedban.bungee.listener;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.bungee.BungeeMain;
import me.leoko.advancedban.bungee.BungeeMethods;
import me.leoko.advancedban.bungee.utils.RedisBungeeHook;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

/**
 * Created by Leoko @ dev.skamps.eu on 24.07.2016.
 */
public class ConnectionListenerBungee implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onConnection(LoginEvent event) {
        if(event.isCancelled())
            return;

        UUIDManager.get().supplyInternUUID(event.getConnection().getName(), event.getConnection().getUniqueId());
        event.registerIntent((BungeeMain)Universal.get().getMethods().getPlugin());
        Universal.get().getMethods().runAsync(() -> {
            // The connection would hang forever if the intent does not get completed
            try {
                String ip = BungeeMethods.getIP(event.getConnection());
                String result = Universal.get().callConnection(event.getConnection().getName(), ip);

                if (result != null) {
                    event.setCancelled(true);
                    event.setCancelReason(TextComponent.fromLegacyText(result));
                }

                if (Universal.isRedis() && ip != null) {
                    RedisBungeeHook.getApi().sendChannelMessage("advancedban:connection", event.getConnection().getName() + "," + ip);
                }
            } finally {
                event.completeIntent((BungeeMain) Universal.get().getMethods().getPlugin());
            }
        });
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        Universal.get().getMethods().runAsync(() -> {
            if (event.getPlayer() != null) {
                PunishmentManager.get().discard(event.getPlayer().getName());
            }
        });
    }

    @EventHandler
    public void onLogin(final PostLoginEvent event) {
        Universal.get().getMethods().scheduleAsync(() -> {
            if (event.getPlayer().getName().equalsIgnoreCase("Leoko")) {
                if (Universal.get().broadcastLeoko()) {
                    ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText(""));
                    ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText("§c§lAdvancedBan §8§l» §7My creator §c§oLeoko §7just joined the game ^^"));
                    ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText(""));
                } else {
                    event.getPlayer().sendMessage(TextComponent.fromLegacyText("§c§lAdvancedBan v3 §8§l» §cHey Leoko we are using your Plugin (NO-BC)"));
                }
            }
        }, 20);
    }
}