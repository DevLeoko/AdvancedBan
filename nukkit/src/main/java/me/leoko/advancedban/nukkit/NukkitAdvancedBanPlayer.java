package me.leoko.advancedban.nukkit;

import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerKickEvent;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanPlayer;

import java.net.InetSocketAddress;
import java.util.UUID;

public class NukkitAdvancedBanPlayer extends NukkitAdvancedBanCommandSender implements AdvancedBanPlayer {

    private final Player player;
    private final InetSocketAddress address;

    public NukkitAdvancedBanPlayer(Player player, AdvancedBan advancedBan) {
        super(player, advancedBan);
        this.player = player;
        this.address = new InetSocketAddress(player.getAddress(), player.getPort());
    }

    @Override
    public InetSocketAddress getAddress() {
        return address;
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public void kick(String reason) {
        player.kick(PlayerKickEvent.Reason.UNKNOWN, reason, false);
    }
}
