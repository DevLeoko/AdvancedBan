package me.leoko.advancedban.bungee.listener;

import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.RequiredArgsConstructor;
import me.leoko.advancedban.bungee.BungeeAdvancedBan;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Base64;

import static me.leoko.advancedban.bungee.RedisMessageUtils.readAddress;
import static net.md_5.bungee.protocol.DefinedPacket.*;

/**
 *
 * @author Beelzebu
 */
@RequiredArgsConstructor
public class PubSubMessageListener implements Listener {
    private static final int CONNECTION = 0;
    private static final int KICK = 1;
    private static final int NOTIFICATION = 2;
    private static final int MESSAGE = 3;

    private final BungeeAdvancedBan advancedBan;

    @EventHandler
    public void onMessageReceive(PubSubMessageEvent e) {
        if (e.getChannel().equals("AdvancedBan")) {
            ByteBuf buf = null;
            try {
                buf = Unpooled.wrappedBuffer(Base64.getDecoder().decode(e.getMessage()));

                ProxiedPlayer player;
                switch (readVarInt(buf)) {
                    case CONNECTION:
                        advancedBan.onPreLogin(readString(buf), readUUID(buf), readAddress(buf));
                        break;
                    case KICK:
                        player = advancedBan.getProxy().getPlayer(readUUID(buf));
                        if (player != null) {
                            player.disconnect(TextComponent.fromLegacyText(readString(buf)));
                        }
                        break;
                    case NOTIFICATION:
                        String permission = readString(buf);
                        String notification = readString(buf);
                        advancedBan.getProxy().getPlayers().forEach(proxiedPlayer -> {
                            if (proxiedPlayer.hasPermission(permission)) {
                                proxiedPlayer.sendMessage(TextComponent.fromLegacyText(notification));
                            }
                        });
                        break;
                    case MESSAGE:
                        player = advancedBan.getProxy().getPlayer(readUUID(buf));
                        if (player != null) {
                            player.sendMessage(TextComponent.fromLegacyText(readString(buf)));
                        }
                        break;
                    default:
                        advancedBan.getLogger().warn("Unknown pubsub message received!");
                        break;
                }
            } finally {
                if (buf != null) {
                    buf.release();
                }
            }
        }
    }
}