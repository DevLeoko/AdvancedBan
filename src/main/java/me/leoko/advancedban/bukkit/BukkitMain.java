package me.leoko.advancedban.bukkit;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.bukkit.listener.ChatListener;
import me.leoko.advancedban.bukkit.listener.CommandListener;
import me.leoko.advancedban.bukkit.listener.ConnectionListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitMain extends JavaPlugin {
    private static BukkitMain instance;

    public static BukkitMain get() {
        return instance;
    }

    public void onEnable() {
        instance = this;
        Universal.get().setup(new BukkitMethods());

        ConnectionListener connListener = new ConnectionListener();
        this.getServer().getPluginManager().registerEvents(connListener, this);
        this.getServer().getPluginManager().registerEvents(new ChatListener(), this);
        this.getServer().getPluginManager().registerEvents(new CommandListener(), this);

        for (Player op : Bukkit.getOnlinePlayers()) {
            AsyncPlayerPreLoginEvent apple = new AsyncPlayerPreLoginEvent(op.getName(), op.getAddress().getAddress(), op.getUniqueId());
            connListener.onConnect(apple);
            if (apple.getLoginResult() == AsyncPlayerPreLoginEvent.Result.KICK_BANNED) {
                op.kickPlayer(apple.getKickMessage());
            }
        }
    }

    public void onDisable() {
        Universal.get().shutdown();
    }
}
