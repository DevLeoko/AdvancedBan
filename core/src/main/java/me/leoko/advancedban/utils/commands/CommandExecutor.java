package me.leoko.advancedban.utils.commands;

import me.leoko.advancedban.Universal;

public interface CommandExecutor {
    String getName();

    default boolean hasPermission(String permission) {
        if (hasBasicPermission(permission))
            return true;

        if (Universal.getInstance().getConfig().getBoolean("EnableAllPermissionNodes", false)) {
            while (permission.contains(".")) {
                permission = permission.substring(0, permission.lastIndexOf('.'));
                if (hasBasicPermission(permission + ".all")) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean hasBasicPermission(String permission);

    void sendMessage(String message);
}
