package me.leoko.advancedban.nukkit.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerAsyncPreLoginEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerKickEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import lombok.RequiredArgsConstructor;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.nukkit.NukkitAdvancedBanPlayer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class ConnectionListener implements Listener {
    private final AdvancedBan advancedBan;
    private final Map<UUID, String> banned = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConnect(PlayerAsyncPreLoginEvent event) {
        try {
            advancedBan.onPreLogin(event.getName(), event.getUuid(), InetAddress.getByName(event.getAddress())).ifPresent(reason -> banned.put(event.getUuid(), reason));
        } catch (UnknownHostException e) {
            advancedBan.getLogger().warn("Error whilst resolving player's address");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDisconnect(PlayerQuitEvent event) {
        advancedBan.getPlayer(event.getPlayer().getUniqueId()).ifPresent(advancedBan::onDisconnect);
        String reason = banned.get(event.getPlayer().getUniqueId());
        if (reason != null) {
            event.setQuitMessage("");
            banned.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(final PlayerJoinEvent event) {
        advancedBan.runAsyncTask(() -> {
            advancedBan.onLogin(new NukkitAdvancedBanPlayer(event.getPlayer(), advancedBan));
            event.setJoinMessage("");
            String reason = banned.get(event.getPlayer().getUniqueId());
            if (reason != null) {
                event.getPlayer().kick(PlayerKickEvent.Reason.UNKNOWN, reason, false);
            }
        });
    }
}
