package me.leoko.advancedban.bukkit.listener;

import lombok.RequiredArgsConstructor;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.bukkit.BukkitAdvancedBanPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created by Leoko @ dev.skamps.eu on 16.07.2016.
 */
@RequiredArgsConstructor
public class ConnectionListener implements Listener {
    private final AdvancedBan advancedBan;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConnect(AsyncPlayerPreLoginEvent event) {
        advancedBan.onPreLogin(event.getName(), event.getUniqueId(), event.getAddress()).ifPresent(reason -> event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, reason));
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event){
        advancedBan.getPlayer(event.getPlayer().getUniqueId()).ifPresent(advancedBan::onDisconnect);
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        advancedBan.runAsyncTask(() -> advancedBan.onLogin(new BukkitAdvancedBanPlayer(event.getPlayer(), advancedBan)));
    }
}