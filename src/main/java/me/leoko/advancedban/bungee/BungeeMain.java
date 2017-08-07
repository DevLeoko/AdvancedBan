package me.leoko.advancedban.bungee;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.bungee.listener.ChatListenerBungee;
import me.leoko.advancedban.bungee.listener.ConnectionListenerBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BungeeMain extends Plugin {
    private static BungeeMain instance;
    private final List<String> onlinePlayers = new ArrayList<>();

    public static BungeeMain get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        Universal.get().setup(new BungeeMethods());

        ProxyServer.getInstance().getPluginManager().registerListener(this, new ConnectionListenerBungee());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new ChatListenerBungee());

        ProxyServer.getInstance().getScheduler().schedule(this, () -> {
            onlinePlayers.clear();
            for (ProxiedPlayer proxiedPlayer : ProxyServer.getInstance().getPlayers()) {
                onlinePlayers.add(proxiedPlayer.getName());
            }
        }, 7, 7, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        Universal.get().shutdown();
    }

    public List<String> getOnlinePlayers() {
        return onlinePlayers;
    }
}

