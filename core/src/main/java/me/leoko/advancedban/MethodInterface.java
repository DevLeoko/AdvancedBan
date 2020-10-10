package me.leoko.advancedban;

import me.leoko.advancedban.utils.punishment.Punishment;
import me.leoko.advancedban.utils.tabcompletion.TabCompleter;

import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

/**
 * The Method Interface is used to define universal actions which are dependent on the server software used.
 */
public interface MethodInterface {
    /**
     * Creates and load the different configuration files.
     */
    void loadFiles();

    /**
     * Request JSON from the <code>url</code> and extract the <code>key</code>
     *
     * @param url the url
     * @param key the key
     * @return the value corresponding to the key
     */
    String getFromUrlJson(String url, String key);

    /**
     * Get the advanced ban version.
     *
     * @return the version
     */
    String getVersion();

    /**
     * Get key from a config file at a <code>path</code>.
     *
     * @param file the file
     * @param path the path
     * @return the string [ ]
     */
    String[] getKeys(Object file, String path);

    /**
     * Get the config.yml file
     *
     * @return the config
     */
    Object getConfig();

    /**
     * Get the messages.yml file
     *
     * @return the messages
     */
    Object getMessages();

    /**
     * Get the layouts.yml file
     *
     * @return the layouts
     */
    Object getLayouts();

    /**
     * Set up metrics.
     */
    void setupMetrics();

    boolean isBungee();

    String clearFormatting(String text);

    /**
     * Get the plugin instance.
     *
     * @return the plugin
     */
    Object getPlugin();

    /**
     * Get the data folder for string punishments.
     *
     * @return the data folder
     */
    File getDataFolder();

    /**
     * Register a command name to be handled by advancedban.
     *
     * @param cmd the cmd
     * @param tabCompleter behaviour when tab completion is triggered
     */
    void setCommandExecutor(String cmd, TabCompleter tabCompleter);

    /**
     * Send a message to a specific player.
     *
     * @param player the player
     * @param msg    the msg
     */
    void sendMessage(Object player, String msg);

    /**
     * Get a players name.
     *
     * @param player the player
     * @return the name
     */
    String getName(Object player);

    /**
     * Get a players name based on the intern uuid fetcher.
     *
     * @param uuid the uuid
     * @return the name
     */
    String getName(String uuid);

    /**
     * Get a players ip.
     *
     * @param player the player
     * @return the ip
     */
    String getIP(Object player);

    /**
     * Get a players uuid based on the intern uuid fetcher.
     *
     * @param player the player
     * @return the intern uuid
     */
    String getInternUUID(Object player);

    /**
     * Get a players uuid based on the intern uuid fetcher.
     *
     * @param player the player
     * @return the intern uuid
     */
    String getInternUUID(String player);

    /**
     * Check if player has the given permission.
     *
     * @param player the player
     * @param perms  the perms
     * @return the boolean
     */
    boolean hasPerms(Object player, String perms);

    /**
     * Check if an offline player has the given permission.
     *
     * @param name server intern identifier for player
     * @param perms  the perms
     * @return the boolean
     */
    boolean hasOfflinePerms(String name, String perms);

    /**
     * Check whether player is online.
     *
     * @param name the name
     * @return the boolean
     */
    boolean isOnline(String name);

    /**
     * Get online player by name.
     *
     * @param name the name
     * @return the player
     */
    Object getPlayer(String name);

    /**
     * Kick a player.
     *
     * @param player the player
     * @param reason the reason
     */
    void kickPlayer(String player, String reason);

    /**
     * Get online players.
     *
     * @return the object [ ]
     */
    Object[] getOnlinePlayers();

    /**
     * Schedule async repeating task.
     *
     * @param rn the rn
     * @param l1 the l 1
     * @param l2 the l 2
     */
    void scheduleAsyncRep(Runnable rn, long l1, long l2);

    /**
     * Schedule async task.
     *
     * @param rn the rn
     * @param l1 the l 1
     */
    void scheduleAsync(Runnable rn, long l1);

    /**
     * Run async task.
     *
     * @param rn the rn
     */
    void runAsync(Runnable rn);

    /**
     * Run sync task.
     *
     * @param rn the rn
     */
    void runSync(Runnable rn);

    /**
     * (see implementation)
     *
     * @param cmd the cmd
     */
    void executeCommand(String cmd);

    /**
     * (see implementation)
     *
     * @param player the player
     * @return the boolean
     */
    boolean callChat(Object player);

    /**
     * (see implementation)
     *
     * @param player the player
     * @param cmd    the cmd
     * @return the boolean
     */
    boolean callCMD(Object player, String cmd);

    /**
     * Get MySQL.yml file.
     *
     * @return the my sql file
     */
    Object getMySQLFile();

    /**
     * Parse json string and retrieve value at given key.
     *
     * @param json the json
     * @param key  the key
     * @return the string
     */
    String parseJSON(InputStreamReader json, String key);

    /**
     * Parse json string and retrieve value at given key.
     *
     * @param json the json
     * @param key  the key
     * @return the string
     */
    String parseJSON(String json, String key);

    /**
     * Get file name.
     *
     * @param file the file
     * @return the file name
     */
    String getFileName(Object file);

    /**
     * Call punishment event.
     *
     * @param punishment the punishment
     */
    void callPunishmentEvent(Punishment punishment);

    /**
     * Call revoke punishment event.
     *
     * @param punishment the punishment
     * @param massClear  the mass clear
     */
    void callRevokePunishmentEvent(Punishment punishment, boolean massClear);

    /**
     * Check whether server is in online mode.
     * Should indicate whether the intern uuid fetcher is going to work.
     *
     * @return the boolean
     */
    boolean isOnlineMode();

    /**
     * Broadcast a message to every user with the given permission.
     *
     * @param perm         the perm
     * @param notification the notification
     */
    void notify(String perm, List<String> notification);

    /**
     * Log a message.
     *
     * @param msg the msg
     */
    void log(String msg);

    /**
     * Whether this instance is used for unit testing.
     *
     * @return the boolean
     */
    boolean isUnitTesting();
}