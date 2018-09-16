package me.leoko.advancedban.bungee.listener;

import lombok.RequiredArgsConstructor;
import me.leoko.advancedban.bungee.BungeeAdvancedBanPlayer;
import me.leoko.advancedban.bungee.BungeeAdvancedBanPlugin;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

/**
 * Created by Leoko @ dev.skamps.eu on 24.07.2016.
 */
@RequiredArgsConstructor
public class ConnectionListener implements Listener {

    private final BungeeAdvancedBanPlugin plugin;

    @EventHandler(priority = EventPriority.LOW)
    public void onConnect(PreLoginEvent event) {
        event.registerIntent(plugin);
        plugin.getAdvancedBan().runAsyncTask(() -> {
            plugin.getAdvancedBan().onPreLogin(event.getConnection().getName(), event.getConnection().getUniqueId(),
                    event.getConnection().getAddress().getAddress()).ifPresent(result -> {
                event.setCancelled(true);
                event.setCancelReason(TextComponent.fromLegacyText(result));
            });
            event.completeIntent(plugin);
        });
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        plugin.getAdvancedBan().runAsyncTask(() ->
                plugin.getAdvancedBan().getPlayer(event.getPlayer().getUniqueId()).ifPresent(advancedBanPlayer ->
                        plugin.getAdvancedBan().onDisconnect(advancedBanPlayer)));
    }

    @EventHandler
    public void onLogin(final PostLoginEvent event) {
        plugin.getAdvancedBan().runAsyncTask(() ->
                plugin.getAdvancedBan().onLogin(new BungeeAdvancedBanPlayer(event.getPlayer(), plugin.getAdvancedBan(),
                        plugin.getProxy())));
    }
}