package me.leoko.advancedban.utils.abstraction;

import java.util.List;

public interface YamlFile {
    /**
     * Get boolean at the given path from the given file.
     *
     * @param path the path
     * @return the boolean
     */
    Boolean getBoolean(String path);

    /**
     * Get string at the given path from the given file.
     *
     * @param path the path
     * @return the string
     */
    String getString(String path);

    /**
     * Get long at the given path from the given file.
     *
     * @param path the path
     * @return the long
     */
    Long getLong(String path);

    /**
     * Get integer at the given path from the given file.
     *
     * @param path the path
     * @return the integer
     */
    Integer getInteger(String path);

    /**
     * Get string list at the given path from the given file.
     *
     * @param path the path
     * @return the string list
     */
    List<String> getStringList(String path);

    /**
     * Get boolean at the given path from the given file or default if not present.
     *
     * @param path the path
     * @param def  the def
     * @return the boolean
     */
    boolean getBoolean(String path, boolean def);

    /**
     * Get string at the given path from the given file or default if not present.
     *
     * @param path the path
     * @param def  the def
     * @return the string
     */
    String getString(String path, String def);

    /**
     * Get long at the given path from the given file or default if not present.
     *
     * @param path the path
     * @param def  the def
     * @return the long
     */
    long getLong(String path, long def);

    /**
     * Get integer at the given path from the given file or default if not present.
     *
     * @param path the path
     * @param def  the def
     * @return the integer
     */
    int getInteger(String path, int def);

    /**
     * Check whether file contains given path.
     *
     * @param path the path
     * @return the boolean
     */
    boolean contains(String path);
}
