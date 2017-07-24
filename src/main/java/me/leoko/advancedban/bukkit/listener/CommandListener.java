package me.leoko.advancedban.bukkit.listener;

import me.leoko.advancedban.Universal;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Created by Leoko @ dev.skamps.eu on 16.07.2016.
 */
public class CommandListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (Universal.get().getMethods().callCMD(event.getPlayer(), event.getMessage())) {
            event.setCancelled(true);
        }
    }
}
