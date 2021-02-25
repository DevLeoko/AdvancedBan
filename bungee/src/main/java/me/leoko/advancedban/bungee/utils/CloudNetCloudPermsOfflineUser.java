package me.leoko.advancedban.bungee.utils;

import me.leoko.advancedban.utils.Permissionable;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.permission.IPermissionUser;

import java.util.UUID;

public class CloudNetCloudPermsOfflineUser implements Permissionable {
    private IPermissionUser permissionUser;

    public CloudNetCloudPermsOfflineUser(String name) {
        this.permissionUser = CloudNetDriver.getInstance().getPermissionManagement().getUsers(name).get(0);
    }

    @Override
    public boolean hasPermission(String permission) {
        return permissionUser != null && permissionUser.hasPermission(permission).asBoolean();
    }
}
