package me.leoko.advancedban.bukkit.listener;

import lombok.RequiredArgsConstructor;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Optional;

/**
 * Created by Leoko @ dev.skamps.eu on 16.07.2016.
 */
@RequiredArgsConstructor
public class MessageListener implements Listener {
    private final AdvancedBan advancedBan;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Optional<AdvancedBanPlayer> player = advancedBan.getPlayer(event.getPlayer().getUniqueId());
        if (player.isPresent() && advancedBan.onChat(player.get(), event.getMessage())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Optional<AdvancedBanPlayer> player = advancedBan.getPlayer(event.getPlayer().getUniqueId());
        if (player.isPresent() && advancedBan.onCommand(player.get(), event.getMessage())) {
            event.setCancelled(true);
        }
    }
}