package me.leoko.advancedban.bungee.listener;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.bungee.BungeeMain;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Created by Leoko @ dev.skamps.eu on 24.07.2016.
 */
public class ChatListenerBungee implements Listener {

    @EventHandler
    public void onChat(ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }
        if (!event.isCommand()) {
            if (Universal.get().getMethods().callChat(event.getSender())) {
                event.setCancelled(true);
            }
        } else {
            if (Universal.get().getMethods().callCMD(event.getSender(), event.getMessage())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        String partialPlayerName = event.getCursor().toLowerCase();

        int lastSpaceIndex = partialPlayerName.lastIndexOf(' ');
        if (lastSpaceIndex >= 0) {
            partialPlayerName = partialPlayerName.substring(lastSpaceIndex + 1);
        }

        for (ProxiedPlayer p : BungeeMain.get().getProxy().getPlayers()) {
            if (p.getName().toLowerCase().startsWith(partialPlayerName)) {
                event.getSuggestions().add(p.getName());
            }
        }
    }
}
