package me.leoko.advancedban.bungee;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.bungee.listener.ChatListenerBungee;
import me.leoko.advancedban.bungee.listener.ConnectionListenerBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeMain extends Plugin{
    private static BungeeMain instance;
    public static BungeeMain get(){
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        Universal.get().setup(new BungeeMethods());

        ProxyServer.getInstance().getPluginManager().registerListener(this, new ConnectionListenerBungee());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new ChatListenerBungee());
    }

    @Override
    public void onDisable(){
        Universal.get().shutdown();
    }
}

