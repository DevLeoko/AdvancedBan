package me.leoko.advancedban.bungee;

import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetSocketAddress;
import java.util.UUID;

public class BungeeAdvancedBanPlayer extends BungeeAdvancedBanCommandSender implements AdvancedBanPlayer {
    private final ProxiedPlayer player;

    public BungeeAdvancedBanPlayer(ProxiedPlayer player, AdvancedBan advancedBan, ProxyServer proxy) {
        super(player, advancedBan, proxy);
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
        player.disconnect(reason);
    }
}
