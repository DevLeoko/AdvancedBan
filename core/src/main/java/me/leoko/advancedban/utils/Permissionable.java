package me.leoko.advancedban.utils;

/**
 * The Permissionable interface defines a way to query whether the implementing object has a certain permission.
 */
@FunctionalInterface
public interface Permissionable {
    /**
     * Checks whether the implementing object has the given permission
     *
     * @param permission the permission to check
     * @return the result
     */
    boolean hasPermission(String permission);
}
