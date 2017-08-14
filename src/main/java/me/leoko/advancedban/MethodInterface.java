package me.leoko.advancedban;

import me.leoko.advancedban.utils.Punishment;

import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by Leoko @ dev.skamps.eu on 23.07.2016.
 */
public interface MethodInterface {
    void loadFiles();

    String getFromUrlJson(String url, String key);

    String getVersion();

    String[] getKeys(Object file, String path);

    Object getConfig();

    Object getMessages();

    Object getLayouts();

    void setupMetrics();

    Object getPlugin();

    File getDataFolder();

    void setCommandExecutor(String cmd);

    void sendMessage(Object player, String msg);

    String getName(Object player);

    String getName(String uuid);

    String getIP(Object player);

    String getInternUUID(Object player);

    String getInternUUID(String player);

    boolean hasPerms(Object player, String perms);

    boolean isOnline(String name);

    Object getPlayer(String name);

    void kickPlayer(String player, String reason);

    Object[] getOnlinePlayers();

    void scheduleAsyncRep(Runnable rn, long l1, long l2);

    void scheduleAsync(Runnable rn, long l1);

    void runAsync(Runnable rn);

    void runSync(Runnable rn);

    void executeCommand(String cmd);

    boolean callChat(Object player);

    boolean callCMD(Object player, String cmd);

    void loadMySQLFile(File f);

    void createMySQLFile(File f);

    Object getMySQLFile();

    String parseJSON(InputStreamReader json, String key);

    String parseJSON(String json, String key);

    Boolean getBoolean(Object file, String path);

    String getString(Object file, String path);

    Long getLong(Object file, String path);

    Integer getInteger(Object file, String path);

    List<String> getStringList(Object file, String path);

    boolean getBoolean(Object file, String path, boolean def);

    String getString(Object file, String path, String def);

    long getLong(Object file, String path, long def);

    int getInteger(Object file, String path, int def);

    boolean contains(Object file, String path);

    String getFileName(Object file);

    void callPunishmentEvent(Punishment punishment);

    void callRevokePunishmentEvent(Punishment punishment, boolean massClear);

    boolean isOnlineMode();
    
    void notify(String perm, List<String> notification);
}
