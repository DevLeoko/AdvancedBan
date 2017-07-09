package me.leoko.advancedban;

import me.leoko.advancedban.listener.ChatListener;
import me.leoko.advancedban.listener.CommandListener;
import me.leoko.advancedban.listener.ConnectionListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.IOException;

public class BukkitMain extends JavaPlugin {
    private static BukkitMain instance;

    public static BukkitMain get() {
        return instance;
    }

    public void onEnable() {
        instance = this;
        Universal.get().setup(new BukkitMethods());

        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
            System.out.println("[AdvancedBan] MC-Stats >> Connected");
        } catch (IOException e) {
            System.out.println("[AdvancedBan] Failed to send Stats!\n Contact: Leoko4433@gmail.com \n Error Code: AB344");
        }

        ConnectionListener connListener = new ConnectionListener();
        this.getServer().getPluginManager().registerEvents(connListener, this);
        this.getServer().getPluginManager().registerEvents(new ChatListener(), this);
        this.getServer().getPluginManager().registerEvents(new CommandListener(), this);

        for (Player op : Bukkit.getOnlinePlayers()) {
            AsyncPlayerPreLoginEvent apple = new AsyncPlayerPreLoginEvent(op.getName(), op.getAddress().getAddress(), op.getUniqueId());
            connListener.onConnect(apple);
            if (apple.getLoginResult() == AsyncPlayerPreLoginEvent.Result.KICK_BANNED)
                op.kickPlayer(apple.getKickMessage());
        }
    }

    public void onDisable() {
        Universal.get().shutdown();
    }
}
