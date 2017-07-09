package me.leoko.advancedban.listener;

import me.leoko.advancedban.BukkitMain;
import me.leoko.advancedban.Universal;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by Leoko @ dev.skamps.eu on 16.07.2016.
 */
public class ConnectionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConnect(AsyncPlayerPreLoginEvent e) {
        String result = Universal.get().callConnection(e.getName(), e.getAddress().getHostAddress());
        if (result != null) e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, result);
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        Universal.get().getMethods().scheduleAsync(() -> {
            if (e.getPlayer().getName().equalsIgnoreCase("Leoko")) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(BukkitMain.get(), () -> {
                    if (Universal.get().broadcastLeoko()) {
                        Bukkit.broadcastMessage("");
                        Bukkit.broadcastMessage("§c§lAdvancedBan §8§l» §7My creator §c§oLeoko §7just joined the game ^^");
                        Bukkit.broadcastMessage("");
                    } else
                        e.getPlayer().sendMessage("§c§lAdvancedBan v2 §8§l» §cHey Leoko we are using your Plugin (NO-BC)");
                }, 20);
            }
        }, 20);
    }


}
