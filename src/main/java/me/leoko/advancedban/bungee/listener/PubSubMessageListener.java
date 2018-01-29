package me.leoko.advancedban.bungee.listener;

import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
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
        if (e.getChannel().equals("AdvancedBan")) {
            String[] msg = e.getMessage().split(" ");
            if (e.getMessage().startsWith("kick ")) {
                if (ProxyServer.getInstance().getPlayer(msg[1]) != null) {
                    ProxyServer.getInstance().getPlayer(msg[1]).disconnect(e.getMessage().substring((msg[0] + msg[1]).length() + 2));
                }
            } else if (e.getMessage().startsWith("notification ")) {
                for (ProxiedPlayer pp : ProxyServer.getInstance().getPlayers()) {
                    if (mi.hasPerms(pp, msg[1])) {
                        mi.sendMessage(pp, e.getMessage().substring((msg[0] + msg[1]).length() + 2));
                    }
                }
            } else if (e.getMessage().startsWith("message ")) {
                if (ProxyServer.getInstance().getPlayer(msg[1]) != null) {
                    ProxyServer.getInstance().getPlayer(msg[1]).sendMessage(e.getMessage().substring((msg[0] + msg[1]).length() + 2));
                }
                if (msg[1].equalsIgnoreCase("CONSOLE")) {
                    ProxyServer.getInstance().getConsole().sendMessage(e.getMessage().substring((msg[0] + msg[1]).length() + 2));
                }
            }
        } else if (e.getChannel().equals("AdvancedBanConnection")) {
            String[] msg = e.getMessage().split(",");
            Universal.get().getIps().remove(msg[0].toLowerCase());
            Universal.get().getIps().put(msg[0].toLowerCase(), msg[1]);
        }
    }
}