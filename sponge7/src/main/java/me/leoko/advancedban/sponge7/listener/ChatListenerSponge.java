package me.leoko.advancedban.sponge7.listener;

import me.leoko.advancedban.Universal;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;

public class ChatListenerSponge {

    @Listener
    public void onChat(MessageChannelEvent.Chat event, @Root Player player) {
        if (!event.getMessage().toPlain().startsWith("/")) {
            if (Universal.get().getMethods().callChat(player)) {
                event.setCancelled(true);
            }
        } else if (Universal.get().getMethods().callCMD(player, event.getMessage().toPlain())) {
            event.setCancelled(true);
        }
    }
}
