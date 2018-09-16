package me.leoko.advancedban.bungee.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.bungee.BungeeAdvancedBan;
import me.leoko.advancedban.bungee.event.PunishmentEvent;
import me.leoko.advancedban.bungee.event.RevokePunishmentEvent;
import me.leoko.advancedban.punishment.Punishment;
import me.leoko.advancedban.punishment.PunishmentType;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Beelzebu
 */
@RequiredArgsConstructor
public class InternalListener implements Listener {
    private final BungeeAdvancedBan advancedBan;

    @EventHandler
    public void onPunish(PunishmentEvent e) {
        sendToBukkit("Punish", Collections.singletonList(e.getPunishment().toString()));
    }

    @EventHandler
    public void onUnPunish(RevokePunishmentEvent e) {
        sendToBukkit("Unpunish", Collections.singletonList(e.getPunishment().toString()));
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
                String message = in.readUTF();
                try {
                    PunishInfo info = AdvancedBan.JSON_MAPPER.readValue(message, PunishInfo.class);
                    Punishment punishment = new Punishment(
                            advancedBan,
                            info.getUuid(),
                            info.getName(),
                            info.getOperator(),
                            info.getCalculation(),
                            info.getStart() != 0 ? info.getStart() : advancedBan.getTimeManager().getTime(),
                            advancedBan.getTimeManager().getTime() + info.getEnd(),
                            info.getType()
                    );
                    punishment.setReason(info.getReason());
                    punishment.create(info.isSilent());
                    advancedBan.getLogger().info("A punishment was created using PluginMessaging listener.");
                    advancedBan.getLogger().debug("Punishment created: " + punishment.toString());
                } catch (IOException | NullPointerException ex) {
                    advancedBan.getLogger().warn("An exception as occurred while reading a punishment from plugin messaging channel.");
                    advancedBan.getLogger().debug("Message: " + message);
                    advancedBan.getLogger().logException(ex);
                }
                break;
            default:
                advancedBan.getLogger().debug("Unknown channel for tag \"AdvancedBan\"");
                break;
        }
    }

    public void sendToBukkit(String channel, List<String> messages) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(channel);
        messages.forEach(out::writeUTF);

        advancedBan.getProxy().getServers().values().forEach(serverInfo -> serverInfo.sendData("AdvancedBan", out.toByteArray(), true));
    }

    @Value
    private static class PunishInfo {
        private final UUID uuid;
        private final String name;
        private final String calculation;
        private final String operator;
        private final long start;
        private final long end;
        private final PunishmentType type;
        private final String reason;
        private final boolean silent;

        public String getCalculation() {
            return calculation == null ? "CONSOLE" : calculation;
        }
    }
}
