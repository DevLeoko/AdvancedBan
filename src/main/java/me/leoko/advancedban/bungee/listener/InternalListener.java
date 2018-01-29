package me.leoko.advancedban.bungee.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.List;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.bungee.event.*;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 *
 * @author Beelzebu
 */
public class InternalListener implements Listener {

    private final Universal universal = Universal.get();

    @EventHandler
    public void onPunish(PunishmentEvent e) {
        sendToBukkit("Punish", Arrays.asList(e.getPunishment().toString()));
    }

    @EventHandler
    public void onUnPunish(RevokePunishmentEvent e) {
        sendToBukkit("Unpunish", Arrays.asList(e.getPunishment().toString()));
    }

    @EventHandler
    public void onPluginMessageEvent(PluginMessageEvent e) {
        if (!e.getTag().equals("AdvancedBan")) {
            return;
        }
        if (e.getSender() instanceof ProxiedPlayer) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
        String channel = in.readUTF();
        switch (channel) {
            case "Punish":
                JsonObject punishment = universal.getGson().fromJson(in.readUTF(), JsonObject.class);
                new Punishment(punishment.get("name").getAsString(),
                        UUIDManager.get().getUUID(punishment.get("uuid").getAsString()),
                        punishment.get("reason").getAsString(),
                        punishment.get("operator").getAsString(),
                        PunishmentType.valueOf(punishment.get("punishmenttype").getAsString().toUpperCase()),
                        punishment.get("start").getAsLong(),
                        punishment.get("end").getAsLong(),
                        punishment.get("calculation").getAsString(),
                        -1
                ).create();
                break;
            default:
                universal.debug("Unknown channel for tag \"AdvancedBan\"");
                break;
        }
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