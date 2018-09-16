package me.leoko.advancedban.bungee.listener;

import lombok.RequiredArgsConstructor;
import me.leoko.advancedban.AdvancedBanPlayer;
import me.leoko.advancedban.bungee.BungeeAdvancedBan;
import me.leoko.advancedban.punishment.PunishmentType;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Optional;

/**
 * Created by Leoko @ dev.skamps.eu on 24.07.2016.
 */
@RequiredArgsConstructor
public class MessageListener implements Listener {
    private final BungeeAdvancedBan advancedBan;

    @EventHandler
    public void onChat(ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }
        ProxiedPlayer proxiedPlayer = (ProxiedPlayer) event.getSender();
        Optional<AdvancedBanPlayer> player = advancedBan.getPlayer(proxiedPlayer.getUniqueId());
        if (!player.isPresent()) {
            advancedBan.getLogger().severe("Player not registered within AdvancedBan");
            return;
        }

        if (event.isCommand() && advancedBan.onCommand(player.get(), event.getMessage()) ||
                !event.isCommand() && advancedBan.onChat(player.get(), event.getMessage())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        String cursor = event.getCursor().toLowerCase();
        if (!advancedBan.isAdvancedBanCommand(cursor)) {
            return;
        }
        if (event.getSender() instanceof ProxiedPlayer) { // Check if the player has permission for tab complete
            ProxiedPlayer pp = (ProxiedPlayer) event.getSender();
            boolean denied = false;
            if (!pp.hasPermission("ab.all")) {
                denied = true;
            }
            if (denied) { // If was denied above, try checking for specific punishments.
                for (PunishmentType pt : PunishmentType.values()) {
                    if (pp.hasPermission(pt.getPerms())) { // The player has permission for some punishment, so allow.
                        denied = false;
                        break;
                    }
                }
            }
            if (denied) { // The event was denied, so cancell it and return.
                event.setCancelled(true);
                return;
            }
        }

        String[] split = cursor.split(" ");
        String partialPlayerName = split[split.length - 1];

        for (ProxiedPlayer p : advancedBan.getProxy().getPlayers()) {
            if (p.getName().toLowerCase().startsWith(partialPlayerName)) {
                event.getSuggestions().add(p.getName());
            }
        }
    }
}
