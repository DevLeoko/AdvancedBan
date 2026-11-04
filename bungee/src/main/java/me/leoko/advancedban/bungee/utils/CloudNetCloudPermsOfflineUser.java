package me.leoko.advancedban.bungee.utils;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.utils.Permissionable;

import java.util.List;

/**
 * Offline permissions through the CloudNet v3 CloudPerms module.
 * CloudNet v3 is not published to any maven repository anymore, which is why it's accessed by reflection.
 */
public class CloudNetCloudPermsOfflineUser implements Permissionable {
    private Object permissionUser;
    private java.lang.reflect.Method hasPermission;
    private java.lang.reflect.Method asBoolean;

    public CloudNetCloudPermsOfflineUser(String name) {
        try {
            Class<?> driver = Class.forName("de.dytanic.cloudnet.driver.CloudNetDriver");
            Class<?> management = Class.forName("de.dytanic.cloudnet.driver.permission.IPermissionManagement");
            hasPermission = Class.forName("de.dytanic.cloudnet.driver.permission.IPermissible").getMethod("hasPermission", String.class);
            asBoolean = Class.forName("de.dytanic.cloudnet.driver.permission.PermissionCheckResult").getMethod("asBoolean");

            Object permissionManagement = driver.getMethod("getPermissionManagement").invoke(driver.getMethod("getInstance").invoke(null));
            List<?> users = (List<?>) management.getMethod("getUsers", String.class).invoke(permissionManagement, name);

            if (!users.isEmpty()) {
                this.permissionUser = users.get(0);
            }
        } catch (ReflectiveOperationException exc) {
            Universal.get().debugException(exc);
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        if (permissionUser == null) {
            return false;
        }

        try {
            return (boolean) asBoolean.invoke(hasPermission.invoke(permissionUser, permission));
        } catch (ReflectiveOperationException exc) {
            Universal.get().debugException(exc);
            return false;
        }
    }
}
