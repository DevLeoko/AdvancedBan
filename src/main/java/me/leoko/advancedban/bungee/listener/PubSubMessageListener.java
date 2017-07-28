package me.leoko.advancedban.bungee.listener;

import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import java.util.ArrayList;
import java.util.List;
import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.PunishmentManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 *
 * @author Beelzebu
 */
public class PubSubMessageListener implements Listener {

    private static final MethodInterface mi = Universal.get().getMethods();

    @EventHandler
    public void onMessageReceive(PubSubMessageEvent e) {
        if (e.getChannel().equalsIgnoreCase("AdvancedBan")) {
            String perm = "ab.notify.unknow";
            String[] msg = e.getMessage().split(" ");
            if (e.getMessage().startsWith("notificationperm ")) {
                perm = e.getMessage().substring(17);
            }
            if (e.getMessage().startsWith("kick ")) {
                if (ProxyServer.getInstance().getPlayer(msg[1]) != null) {
                    ProxyServer.getInstance().getPlayer(msg[1]).disconnect(e.getMessage().substring(6 + msg[1].length()));
                }
            } else if (e.getMessage().startsWith("notification ")) {
                for (ProxiedPlayer pp : ProxyServer.getInstance().getPlayers()) {
                    if (mi.hasPerms(perm, perm)) {
                        mi.sendMessage(pp, e.getMessage().substring(13));
                    }
                }
            } else if (e.getMessage().equals("refresh")) {
                Universal.get().getMethods().runAsync(() -> {
                    PunishmentManager.get().refresh();
                });
            }
        }
    }
}
