package me.leoko.advancedban.listener;

import me.leoko.advancedban.Universal;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Created by Leoko @ dev.skamps.eu on 16.07.2016.
 */
public class ChatListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (Universal.get().getMethods().callChat(e.getPlayer())) e.setCancelled(true);
    }
}
