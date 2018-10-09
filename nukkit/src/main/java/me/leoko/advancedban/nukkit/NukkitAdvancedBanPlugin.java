package me.leoko.advancedban.nukkit;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import me.leoko.advancedban.nukkit.listener.ConnectionListener;
import me.leoko.advancedban.nukkit.listener.MessageListener;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NukkitAdvancedBanPlugin extends PluginBase {

    private final NukkitAdvancedBan advancedBan = new NukkitAdvancedBan(this);

    @Override
    public void onEnable() {
        advancedBan.onEnable();
        getServer().getPluginManager().registerEvents(new ConnectionListener(advancedBan), this);
        getServer().getPluginManager().registerEvents(new MessageListener(advancedBan), this);

        for (Player player : getServer().getOnlinePlayers().values()) {
            try {
                advancedBan.onPreLogin(player.getName(), player.getUniqueId(), InetAddress.getByName(player.getAddress())).ifPresent(player::kick);
            } catch (UnknownHostException e) {
                advancedBan.getLogger().warn("Error whilst resolving player's address");
            }
        }
    }

    @Override
    public void onDisable() {
        advancedBan.onDisable();
    }
}
