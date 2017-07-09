package me.leoko.advancedban.bungee.listener;

import me.leoko.advancedban.Universal;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Created by Leoko @ dev.skamps.eu on 24.07.2016.
 */
public class ChatListenerBungee implements Listener {
    @EventHandler
    public void onChat(ChatEvent e) {
        if (e.getSender() instanceof ProxiedPlayer) {
            if (!e.isCommand()) {
                if (Universal.get().getMethods().callChat(e.getSender())) e.setCancelled(true);
            } else {
                if (Universal.get().getMethods().callCMD(e.getSender(), e.getMessage())) e.setCancelled(true);
            }
        }
    }
}
