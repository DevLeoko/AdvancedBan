package me.leoko.advancedban.bungee.utils;

import me.leoko.advancedban.utils.Permissionable;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.permission.IPermissionUser;

import java.util.List;
import java.util.UUID;

public class CloudNetCloudPermsOfflineUser implements Permissionable {
    private IPermissionUser permissionUser;

    public CloudNetCloudPermsOfflineUser(String name) {
    	final List <IPermissionUser> users = CloudNetDriver.getInstance().getPermissionManagement().getUsers(name);
    	
    	if (!users.isEmpty()) {
        	this.permissionUser = users.get(0);
    	} else {
    		this.permissionUser = null;
    	}
    }

    @Override
    public boolean hasPermission(String permission) {
        return permissionUser != null && permissionUser.hasPermission(permission).asBoolean();
    }
}
