package me.leoko.advancedban.bungee.cloud;

import me.leoko.advancedban.bungee.cloud.support.CloudNetV2Support;
import me.leoko.advancedban.bungee.cloud.support.CloudNetV3Support;
import me.leoko.advancedban.bungee.cloud.support.CloudNetV4Support;
import net.md_5.bungee.api.ProxyServer;

public class CloudSupportHandler {

    public static CloudSupport getCloudSystem(){
        try {
            if (ProxyServer.getInstance().getPluginManager().getPlugin("CloudNet-Bridge") != null) {
                Class.forName("de.dytanic.cloudnet.driver.CloudNetDriver");
                return new CloudNetV3Support();
            }
        } catch (ClassNotFoundException ignored) {}
        try {
            if (ProxyServer.getInstance().getPluginManager().getPlugin("CloudNet-Bridge") != null) {
                Class.forName("eu.cloudnetservice.driver.CloudNetDriver");
                return new CloudNetV4Support();
            }
        } catch (ClassNotFoundException ignored) {}
        if (ProxyServer.getInstance().getPluginManager().getPlugin("CloudNetAPI") != null) {
            return new CloudNetV2Support();
        }
        return null;
    }
}
