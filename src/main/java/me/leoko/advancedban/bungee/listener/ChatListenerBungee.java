package me.leoko.advancedban.bungee.listener;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.utils.Command;
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
        final String commandName = event.getCursor().split(" ")[0];
        if (commandName.length() > 1 && event.getCursor().length() > commandName.length()) {
            final Command command = Command.getByName(commandName.substring(1));
            if (command != null && event.getSender() instanceof ProxiedPlayer) {
                if (command.getPermission() == null || Universal.get().getMethods().hasPerms(event.getSender(), command.getPermission())) {
                    final String[] args = event.getCursor().substring(commandName.length() + 1).split(" ", -1);
                    event.getSuggestions().addAll(command.getTabCompleter().onTabComplete(event.getSender(), args));
                }
            }
        }
    }
}
