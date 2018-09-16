package me.leoko.advancedban.bukkit;

import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanPlayer;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.UUID;

public class BukkitAdvancedBanPlayer extends BukkitAdvancedBanCommandSender implements AdvancedBanPlayer {
    private final Player player;

    public BukkitAdvancedBanPlayer(Player player, AdvancedBan advancedBan) {
        super(player, advancedBan);
        this.player = player;
    }

    @Override
    public InetSocketAddress getAddress() {
        return player.getAddress();
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public void kick(String reason) {
        player.kickPlayer(reason);
    }
}
