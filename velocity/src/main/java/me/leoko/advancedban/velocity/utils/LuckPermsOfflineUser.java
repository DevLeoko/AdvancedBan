package me.leoko.advancedban.velocity.utils;

import me.leoko.advancedban.utils.Permissionable;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;

import java.util.UUID;

public class LuckPermsOfflineUser implements Permissionable {

    private User permissionUser;

    public LuckPermsOfflineUser(String name) {
        final UserManager userManager = LuckPermsProvider.get().getUserManager();
        final UUID uuid = userManager.lookupUniqueId(name).join();
        if (uuid != null) {
            this.permissionUser = userManager.loadUser(uuid).join();
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        return permissionUser != null && permissionUser.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }
}