package me.leoko.advancedban.nukkit.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import lombok.RequiredArgsConstructor;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanPlayer;

import java.util.Optional;

@RequiredArgsConstructor
public class MessageListener implements Listener {
    private final AdvancedBan advancedBan;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(PlayerChatEvent event) {
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
