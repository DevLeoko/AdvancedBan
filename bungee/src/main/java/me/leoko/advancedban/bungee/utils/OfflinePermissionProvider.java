package me.leoko.advancedban.bungee.utils;

public interface OfflinePermissionProvider {
    void requestOfflinePermissionPlayer(String name);
    void releaseOfflinePermissionPlayer(String name);
    boolean hasOfflinePerms(String name, String perms);
}
