
package me.leoko.advancedban.bungee.cloud.support;

import de.dytanic.cloudnet.api.player.PlayerExecutorBridge;
import de.dytanic.cloudnet.bridge.CloudServer;
import me.leoko.advancedban.bungee.cloud.CloudSupport;

import java.util.UUID;

public class CloudNetV2Support implements CloudSupport {

    @Override
    public void kick(UUID uniqueID, String reason) {
        PlayerExecutorBridge.INSTANCE.kickPlayer(CloudServer.getInstance().getCloudPlayers().get(uniqueID), reason);
    }
}
