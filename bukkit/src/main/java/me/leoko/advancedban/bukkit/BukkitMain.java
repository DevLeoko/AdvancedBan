package me.leoko.advancedban.bukkit;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.bukkit.listener.ChatListener;
import me.leoko.advancedban.bukkit.listener.CommandListener;
import me.leoko.advancedban.bukkit.listener.ConnectionListener;
import me.leoko.advancedban.bukkit.listener.InternalListener;
import me.leoko.advancedban.bukkit.placeholder.AdvancedBanExpansion;
import me.leoko.advancedban.manager.UUIDManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class BukkitMain extends JavaPlugin {
    private static BukkitMain instance;
    private Runnable unregisterPlaceholders;

    public static BukkitMain get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        Universal.get().setup(new BukkitMethods());

        this.getServer().getPluginManager().registerEvents(new ConnectionListener(), this);
        this.getServer().getPluginManager().registerEvents(new ChatListener(), this);
        this.getServer().getPluginManager().registerEvents(new CommandListener(), this);
        this.getServer().getPluginManager().registerEvents(new InternalListener(), this);

        // Players are already online if the plugin got enabled by a reload. The list gets copied as kicking modifies it.
        for (Player player : new ArrayList<>(Bukkit.getOnlinePlayers())) {
            UUIDManager.get().supplyInternUUID(player.getName(), player.getUniqueId());
            InetSocketAddress address = player.getAddress();
            String result = Universal.get().callConnection(player.getName(), address == null ? null : address.getAddress().getHostAddress());
            if (result != null) {
                player.kickPlayer(result);
            }
        }

        registerPlaceholders();
    }

    private void registerPlaceholders() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                unregisterPlaceholders = AdvancedBanExpansion.registerExpansion(this);
            } catch (LinkageError error) {
                Universal.get().log("This version of PlaceholderAPI is not supported, its placeholders will not work: " + error);
            }
        }
    }

    @Override
    public void onDisable() {
        if (unregisterPlaceholders != null) {
            unregisterPlaceholders.run();
        }
        Universal.get().shutdown();
    }
}