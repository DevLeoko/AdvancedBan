package me.leoko.advancedban;

import me.leoko.advancedban.utils.Punishment;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * Created by Leo on 07.08.2017.
 */
public class TestMethods implements MethodInterface {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File dataFolder;

    public TestMethods(){
        try {
            folder.create();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataFolder = folder.newFolder("base");
    }

    @Override
    public void loadFiles() {
        //No files are setup
    }

    @Override
    public String getFromUrlJson(String url, String key) {
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #1");
        return null;
    }

    @Override
    public String getVersion() {
        return "TEST";
    }

    @Override
    public String[] getKeys(Object file, String path) {
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #2");
        return new String[0];
    }

    @Override
    public Object getConfig() {
        return null;
    }

    @Override
    public Object getMessages() {
        return null;
    }

    @Override
    public Object getLayouts() {
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #5");
        return null;
    }

    @Override
    public void setupMetrics() {
        //No metrics for testing needed
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
    public void setCommandExecutor(String cmd) {
        //Command executors are not being registered for testing
    }

    @Override
    public void sendMessage(Object player, String msg) {
        System.out.println("Message: "+player+" -> "+msg);
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
        return true;
    }

    @Override
    public boolean isOnline(String name) {
        return false;
    }

    @Override
    public Object getPlayer(String name) {
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #10");
        return null;
    }

    @Override
    public void kickPlayer(String player, String reason) {
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #11");

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
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #17");
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
    public void loadMySQLFile(File f) {
        //Nothing to do here while testing
    }

    @Override
    public void createMySQLFile(File f) {
        System.out.println("Created new MySQL-File");
    }

    @Override
    public Object getMySQLFile() {
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #22");
        return null;
    }

    @Override
    public String parseJSON(InputStreamReader json, String key) {
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #23");
        return null;
    }

    @Override
    public String parseJSON(String json, String key) {
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #24");
        return null;
    }

    @Override
    public Boolean getBoolean(Object file, String path) {
        fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #25");
        return null;
    }

    @Override
    public String getString(Object file, String path) {
        return path;
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
        return new ArrayList<>();
    }

    @Override
    public boolean getBoolean(Object file, String path, boolean def) {
        if(path.equals("DetailedEnableMessage")
                || path.equals("UUID-Fetcher.Enabled")
                || path.equals("DetailedDisableMessage")) return false;
        return def;
    }

    @Override
    public String getString(Object file, String path, String def) {
        return def;
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
        return true;
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
        return false;
    }

    @Override
    public void notify(String perm, List<String> notification) {
        notification.forEach(System.out::println);
    }
}
