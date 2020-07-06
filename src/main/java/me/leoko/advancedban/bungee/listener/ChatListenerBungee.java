package me.leoko.advancedban.bungee.listener;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.bungee.BungeeMain;
import me.leoko.advancedban.utils.PunishmentType;
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
        String cursor = event.getCursor().toLowerCase();
        if (!(cursor.startsWith("/ban ") || cursor.startsWith("/ban-ip ") || cursor.startsWith("/banip ")
                || cursor.startsWith("/check ") || cursor.startsWith("/history ") || cursor.startsWith("/ipban ")
                || cursor.startsWith("/kick ") || cursor.startsWith("/mute ") || cursor.startsWith("/tempban ")
                || cursor.startsWith("/tempipban ") || cursor.startsWith("/tempmute ") || cursor.startsWith("/tempwarn ")
                || cursor.startsWith("/tipban ") || cursor.startsWith("/unmute ") || cursor.startsWith("/unwarn ")
                || cursor.startsWith("/warn ")) || cursor.startsWith("/unnote ") || cursor.startsWith("/note ")) {
            return;
        }
        if (event.getSender() instanceof ProxiedPlayer) { // Check if the player has permission for tab complete
            ProxiedPlayer pp = (ProxiedPlayer) event.getSender();
            boolean deny = false;
            if (!Universal.get().hasPerms(pp, "ab.all")) {
                deny = true;
            }
            if (deny) { // If was denied above, try checking for specific punishments.
                for (PunishmentType pt : PunishmentType.values()) {
                    if (pp.hasPermission(pt.getPerms())) { // The player has permission for some punishment, so allow.
                        deny = false;
                        break;
                    }
                }
            }
            if (deny) { // The event was denied, so cancell it and return.
                event.setCancelled(true);
                return;
            }
        }

        String[] split = cursor.split(" ");
        String partialPlayerName = split[split.length - 1];

        for (ProxiedPlayer p : BungeeMain.get().getProxy().getPlayers()) {
            if (p.getName().toLowerCase().startsWith(partialPlayerName)) {
                event.getSuggestions().add(p.getName());
            }
        }
    }
}
