package me.leoko.advancedban.bukkit;

import me.leoko.advancedban.bukkit.listener.ConnectionListener;
import me.leoko.advancedban.bukkit.listener.InternalListener;
import me.leoko.advancedban.bukkit.listener.MessageListener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitAdvancedBanPlugin extends JavaPlugin {

    private final BukkitAdvancedBan advancedBan = new BukkitAdvancedBan(this);

    @Override
    public void onEnable() {

        advancedBan.onEnable();
        getServer().getPluginManager().registerEvents(new ConnectionListener(advancedBan), this);
        getServer().getPluginManager().registerEvents(new MessageListener(advancedBan), this);
        getServer().getPluginManager().registerEvents(new InternalListener(advancedBan), this);

        for (Player player : getServer().getOnlinePlayers()) {
            advancedBan.onPreLogin(player.getName(), player.getUniqueId(), player.getAddress().getAddress()).ifPresent(player::kickPlayer);
        }
    }

    @Override
    public void onDisable() {
        advancedBan.onDisable();
    }
}