package me.leoko.advancedban;

import me.leoko.advancedban.utils.Permissionable;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.tabcompletion.TabCompleter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Created by Leo on 07.08.2017.
 */
public class TestMethods implements MethodInterface {
	private final File dataFolder;

	// Allows tests to set the scene and to look at the outcome
	final Set<String> onlinePlayers = new HashSet<>();
	final List<String> exemptPlayers = new ArrayList<>();
	final Map<String, List<String>> layouts = new HashMap<>();
	final List<String> sentMessages = new ArrayList<>();
	final List<String> notifications = new ArrayList<>();
	final List<String> kickedPlayers = new ArrayList<>();
	final List<String> executedCommands = new ArrayList<>();
	final Map<String, Boolean> booleans = new HashMap<>();
	final Set<String> offlinePermissions = new HashSet<>();
	boolean unitTesting = true;
	boolean onlineMode = false;
	boolean brokenMessages = false;
	int loadFilesCalls = 0;

    public TestMethods(File dataFolder){
    	this.dataFolder = Objects.requireNonNull(dataFolder);
    }

    @Override
    public void loadFiles() {
        //No files are setup
        loadFilesCalls++;
    }

    @Override
    public String getVersion() {
        return "TEST";
    }

    @Override
    public String[] getKeys(Object file, String path) {
        // e.g. "Ladder" with the entry "Ladder.Spam.Steps" has the key "Spam"
        return layouts.keySet().stream()
                .filter(key -> key.startsWith(path + "."))
                .map(key -> key.substring(path.length() + 1).split("\\.")[0])
                .distinct()
                .toArray(String[]::new);
    }

    @Override
    public Object getConfig() {
        return null;
    }

    @Override
    public Object getMessages() {
        if (brokenMessages) {
            throw new IllegalStateException("Broken messages");
        }
        return null;
    }

    @Override
    public Object getLayouts() {
        return null;
    }

    @Override
    public void setupMetrics() {
        //No metrics for testing needed
    }

    @Override
    public boolean isBungee() {
        return false;
    }

    @Override
    public String clearFormatting(String text) {
        return text;
    }

    @Override
    public Object getPlugin() {
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #6");
        return null;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public void setCommandExecutor(String cmd, String permission, TabCompleter tabCompleter) {
        //Command executors are not being registered for testing
    }

    @Override
    public void sendMessage(Object player, String msg) {
        System.out.println("Message: "+player+" -> "+msg);
        sentMessages.add(msg);
    }

    @Override
    public String getName(Object player) {
        return player.toString();
    }

    @Override
    public String getName(String uuid) {
        return uuid;
    }

    @Override
    public String getIP(Object player) {
        return "127.0.0.1";
    }

    @Override
    public String getInternUUID(Object player) {
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #7");
        return null;
    }

    @Override
    public String getInternUUID(String player) {
        return null;
    }

    @Override
    public boolean hasPerms(Object player, String perms) {
        // Only the player executing the commands has permissions, punished players do not
        return !onlinePlayers.contains(player);
    }

    @Override
    public Permissionable getOfflinePermissionPlayer(String name) {
        return permission -> offlinePermissions.contains(name + ":" + permission);
    }

    @Override
    public boolean isOnline(String name) {
        return onlinePlayers.contains(name.toLowerCase());
    }

    @Override
    public Object getPlayer(String name) {
        return onlinePlayers.contains(name.toLowerCase()) ? name.toLowerCase() : null;
    }

    @Override
    public void kickPlayer(String player, String reason) {
        kickedPlayers.add(player.toLowerCase());
    }

    @Override
    public Object[] getOnlinePlayers() {
        return new Object[0];
    }

    @Override
    public void scheduleAsyncRep(Runnable rn, long l1, long l2) {
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #13");
    }

    @Override
    public void scheduleAsync(Runnable rn, long l1) {
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #14");
    }

    @Override
    public void runAsync(Runnable rn) {
        rn.run(); //Keeping it sync for unit test
    }

    @Override
    public void runSync(Runnable rn) {
        rn.run();
    }

    @Override
    public void executeCommand(String cmd) {
        executedCommands.add(cmd);
    }

    @Override
    public boolean callChat(Object player) {
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #18");
        return false;
    }

    @Override
    public boolean callCMD(Object player, String cmd) {
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #19");
        return false;
    }

    @Override
    public Object getMySQLFile() {
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #22");
        return null;
    }

    @Override
    public Boolean getBoolean(Object file, String path) {
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #25");
        return null;
    }

    @Override
    public String getString(Object file, String path) {
        List<String> layout = layouts.get(path);
        return layout == null ? path : layout.get(0);
    }

    @Override
    public Long getLong(Object file, String path) {
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #27");
        return null;
    }

    @Override
    public Integer getInteger(Object file, String path) {
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #28");
        return null;
    }

    @Override
    public List<String> getStringList(Object file, String path) {
        if (path.equals("ExemptPlayers")) {
            return exemptPlayers;
        }
        return layouts.getOrDefault(path, new ArrayList<>());
    }

    @Override
    public boolean getBoolean(Object file, String path, boolean def) {
        if(path.equals("DetailedEnableMessage")
                || path.equals("UUID-Fetcher.Enabled")
                || path.equals("DetailedDisableMessage")) return false;

        if(path.equals("Debug")) return true;

        return booleans.getOrDefault(path, def);
    }

    @Override
    public String getString(Object file, String path, String def) {
        List<String> layout = layouts.get(path);
        return layout == null ? def : layout.get(0);
    }

    @Override
    public long getLong(Object file, String path, long def) {
        return def;
    }

    @Override
    public int getInteger(Object file, String path, int def) {
        return def;
    }

    @Override
    public boolean contains(Object file, String path) {
        // Layouts only exist if a test has defined them, everything else claims to be there
        return !path.startsWith("Message.") || layouts.containsKey(path);
    }

    @Override
    public String getFileName(Object file) {
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #35");
        return null;
    }

    @Override
    public void callPunishmentEvent(Punishment punishment) {
        System.out.println("Called punishment event!");
    }

    @Override
    public void callRevokePunishmentEvent(Punishment punishment, boolean massClear) {
        System.out.println("Called punishment-revoke event!");
    }

    @Override
    public boolean isOnlineMode() {
        return onlineMode;
    }

    @Override
    public void notify(String perm, List<String> notification) {
        notification.forEach(System.out::println);
        notifications.addAll(notification);
    }

    @Override
    public void log(String msg) {
        System.out.println("Logging: " + msg);
    }

    @Override
    public boolean isUnitTesting() {
        return unitTesting;
    }
}