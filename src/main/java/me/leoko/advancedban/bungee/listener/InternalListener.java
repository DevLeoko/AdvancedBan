package me.leoko.advancedban.bungee.listener;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.Arrays;
import java.util.List;
import me.leoko.advancedban.bungee.event.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 *
 * @author Beelzebu
 */
public class InternalListener implements Listener {

    @EventHandler
    public void onPunish(PunishmentEvent e) {
        sendToBukkit("Punish", Arrays.asList(e.getPunishment().toString()));
    }

    @EventHandler
    public void onUnPunish(RevokePunishmentEvent e) {
        sendToBukkit("Unpunish", Arrays.asList(e.getPunishment().toString()));
    }

    public void sendToBukkit(String channel, List<String> messages) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(channel);
        messages.forEach((msg) -> {
            out.writeUTF(msg);
        });
        ProxyServer.getInstance().getServers().keySet().forEach(server -> {
            ProxyServer.getInstance().getServerInfo(server).sendData("AdvancedBan", out.toByteArray(), true);
        });
    }
}
