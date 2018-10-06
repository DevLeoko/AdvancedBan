package me.leoko.advancedban.bungee;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import me.leoko.advancedban.bungee.listener.ConnectionListener;
import me.leoko.advancedban.bungee.listener.MessageListener;
import me.leoko.advancedban.bungee.listener.PubSubMessageListener;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeAdvancedBanPlugin extends Plugin {

    private final BungeeAdvancedBan advancedBan = new BungeeAdvancedBan(this);

    @Override
    public void onEnable() {
        advancedBan.onEnable();
        getProxy().getPluginManager().registerListener(this, new ConnectionListener(this));
        getProxy().getPluginManager().registerListener(this, new MessageListener(advancedBan));
        getProxy().registerChannel("AdvancedBan");
        if (getProxy().getPluginManager().getPlugin("RedisBungee") != null) {
            RedisBungeeAPI redisBungee = RedisBungee.getApi();
            advancedBan.setRedisBungee(redisBungee);
            getProxy().getPluginManager().registerListener(this, new PubSubMessageListener(advancedBan));
            redisBungee.registerPubSubChannels("AdvancedBan", "AdvancedBanConnection");
            advancedBan.getLogger().info("RedisBungee detected, hooking into it!");
        }
    }

    @Override
    public void onDisable() {
        advancedBan.onDisable();
    }

    public BungeeAdvancedBan getAdvancedBan() {
        return advancedBan;
    }
}