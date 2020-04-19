package me.leoko.advancedban;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import me.leoko.advancedban.bungee.BungeeMethods;
import me.leoko.advancedban.manager.*;
import me.leoko.advancedban.utils.InterimData;
import me.leoko.advancedban.utils.Punishment;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * This is the server independent entry point of the plugin.
 */
public class Universal {

    private static Universal instance = null;
    private final Map<String, String> ips = new HashMap<>();
    private MethodInterface mi;
    private LogManager logManager;
    private static boolean redis = false;
    private final Gson gson = new Gson();

    /**
     * Get universal.
     *
     * @return the universal instance
     */
    public static Universal get() {
        return instance == null ? instance = new Universal() : instance;
    }

    /**
     * Initially sets up the plugin.
     *
     * @param mi the mi
     */
    public void setup(MethodInterface mi) {
        this.mi = mi;
        mi.loadFiles();
        logManager = new LogManager();
        UpdateManager.get().setup();
        UUIDManager.get().setup();

        try {
            DatabaseManager.get().setup(mi.getBoolean(mi.getConfig(), "UseMySQL", false));
        } catch (Exception ex) {
            log("Failed enabling database-manager...");
            debug(ex.getMessage());
        }

        mi.setupMetrics();
        PunishmentManager.get().setup();

        mi.setCommandExecutor("advancedban");
        mi.setCommandExecutor("change-reason");
        mi.setCommandExecutor("ban");
        mi.setCommandExecutor("tempban");
        mi.setCommandExecutor("ipban");
        mi.setCommandExecutor("ban-ip");
        mi.setCommandExecutor("banip");
        mi.setCommandExecutor("tempipban");
        mi.setCommandExecutor("tipban");
        mi.setCommandExecutor("banip");
        mi.setCommandExecutor("kick");
        mi.setCommandExecutor("warn");
        mi.setCommandExecutor("tempwarn");
        mi.setCommandExecutor("mute");
        mi.setCommandExecutor("tempmute");
        mi.setCommandExecutor("unmute");
        mi.setCommandExecutor("unwarn");
        mi.setCommandExecutor("unban");
        mi.setCommandExecutor("banlist");
        mi.setCommandExecutor("history");
        mi.setCommandExecutor("warns");
        mi.setCommandExecutor("check");
        mi.setCommandExecutor("systemprefs");
        mi.setCommandExecutor("unpunish");

        String upt = "You have the newest version";
        String response = getFromURL("https://api.spigotmc.org/legacy/update.php?resource=8695");
        if (response == null) {
            upt = "Failed to check for updates :(";
        } else if ((!mi.getVersion().startsWith(response))) {
            upt = "There is a new version available! [" + response + "]";
        }

        if (mi.getBoolean(mi.getConfig(), "DetailedEnableMessage", true)) {
            mi.log("\n \n&8[]=====[&7Enabling AdvancedBan&8]=====[]"
                    + "\n&8| &cInformation:"
                    + "\n&8|   &cName: &7AdvancedBan"
                    + "\n&8|   &cDeveloper: &7Leoko"
                    + "\n&8|   &cVersion: &7" + mi.getVersion()
                    + "\n&8|   &cStorage: &7" + (DatabaseManager.get().isUseMySQL() ? "MySQL (external)" : "HSQLDB (local)")
                    + "\n&8| &cSupport:"
                    + "\n&8|   &cGithub: &7https://github.com/DevLeoko/AdvancedBan/issues"
                    + "\n&8|   &cDiscord: &7https://discord.gg/ycDG6rS"
                    + "\n&8| &cTwitter: &7@LeokoGar"
                    + "\n&8| &cUpdate:"
                    + "\n&8|   &7" + upt
                    + "\n&8[]================================[]&r\n ");
        } else {
            mi.log("&cEnabling AdvancedBan on Version &7" + mi.getVersion());
            mi.log("&cCoded by &7Leoko &8| &7Twitter: @LeokoGar");
        }
    }

    /**
     * Shutdown.
     */
    public void shutdown() {
        DatabaseManager.get().shutdown();

        if (mi.getBoolean(mi.getConfig(), "DetailedDisableMessage", true)) {
            mi.log("\n \n&8[]=====[&7Disabling AdvancedBan&8]=====[]"
                    + "\n&8| &cInformation:"
                    + "\n&8|   &cName: &7AdvancedBan"
                    + "\n&8|   &cDeveloper: &7Leoko"
                    + "\n&8|   &cVersion: &7" + getMethods().getVersion()
                    + "\n&8|   &cStorage: &7" + (DatabaseManager.get().isUseMySQL() ? "MySQL (external)" : "HSQLDB (local)")
                    + "\n&8| &cSupport:"
                    + "\n&8|   &cGithub: &7https://github.com/DevLeoko/AdvancedBan/issues"
                    + "\n&8|   &cDiscord: &7https://discord.gg/ycDG6rS"
                    + "\n&8| &cTwitter: &7@LeokoGar"
                    + "\n&8[]================================[]&r\n ");
        } else {
            mi.log("&cDisabling AdvancedBan on Version &7" + getMethods().getVersion());
            mi.log("&cCoded by Leoko &8| &7Twitter: @LeokoGar");
        }
    }

    /**
     * Gets ips.
     *
     * @return the ips
     */
    public Map<String, String> getIps() {
        return ips;
    }

    /**
     * Gets methods.
     *
     * @return the methods
     */
    public MethodInterface getMethods() {
        return mi;
    }

    /**
     * Is bungee boolean.
     *
     * @return the boolean
     */
    public boolean isBungee() {
        return mi instanceof BungeeMethods;
    }

    /**
     * Gets from url.
     *
     * @param surl the surl
     * @return the from url
     */
    public String getFromURL(String surl) {
        String response = null;
        try {
            URL url = new URL(surl);
            Scanner s = new Scanner(url.openStream());
            if (s.hasNext()) {
                response = s.next();
                s.close();
            }
        } catch (IOException exc) {
            debug("!! Failed to connect to URL: " + surl);
        }
        return response;
    }

    /**
     * Is mute command boolean.
     *
     * @param cmd the cmd
     * @return the boolean
     */
    public boolean isMuteCommand(String cmd) {
        cmd = cmd.contains(":") ? cmd.split(":", 2)[1] : cmd;
        for (String str : getMethods().getStringList(getMethods().getConfig(), "MuteCommands")) {
            if (cmd.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is exempt player boolean.
     *
     * @param name the name
     * @return the boolean
     */
    public boolean isExemptPlayer(String name) {
        List<String> exempt = getMethods().getStringList(getMethods().getConfig(), "ExemptPlayers");
        if (exempt != null) {
            for (String str : exempt) {
                if (name.equalsIgnoreCase(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Broadcast leoko boolean.
     *
     * @return the boolean
     */
    public boolean broadcastLeoko() {
        File readme = new File(getMethods().getDataFolder(), "readme.txt");
        if (!readme.exists()) {
            return true;
        }
        try {
            if (Files.readAllLines(Paths.get(readme.getPath()), Charset.defaultCharset()).get(0).equalsIgnoreCase("I don't want that there will be any message when the dev of this plugin joins the server! I want this even though the plugin is 100% free and the join-message is the only reward for the Dev :(")) {
                return false;
            }
        } catch (IOException ignore) {
        }
        return true;
    }

    /**
     * Call connection string.
     *
     * @param name the name
     * @param ip   the ip
     * @param fullUUID the uuid
     * @return the string
     */
    public String callConnection(String name, String ip, UUID fullUUID) {
        name = name.toLowerCase();
        String uuid = fullUUID.toString().replace("-", "");
        if (uuid == null) {
            return "[AdvancedBan] Failed to fetch your UUID";
        }

        if (ip != null) {
            getIps().remove(name);
            getIps().put(name, ip);
        }

        InterimData interimData = PunishmentManager.get().load(name, uuid, ip);
        Punishment pt = interimData.getBan();

        if (pt == null) {
            interimData.accept();
            return null;
        }

        return pt.getLayoutBSN();
    }

    /**
     * Has perms boolean.
     *
     * @param player the player
     * @param perms  the perms
     * @return the boolean
     */
    public boolean hasPerms(Object player, String perms) {
        if (mi.hasPerms(player, perms)) {
            return true;
        }

        if (mi.getBoolean(mi.getConfig(), "EnableAllPermissionNodes", false)) {
            while (perms.contains(".")) {
                perms = perms.substring(0, perms.lastIndexOf('.'));
                if (mi.hasPerms(player, perms + ".all")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Use redis.
     *
     * @param use the use
     */
    public void useRedis(boolean use) {
        redis = use;
    }

    /**
     * Use redis boolean.
     *
     * @return the boolean
     */
    public boolean useRedis() {
        return redis;
    }

    /**
     * Log.
     *
     * @param msg the msg
     */
    public void log(String msg) {
        mi.log("§8[§cAdvancedBan§8] §7" + msg);
        debugToFile(msg);
    }

    /**
     * Debug.
     *
     * @param msg the msg
     */
    public void debug(Object msg) {
        if (mi.getBoolean(mi.getConfig(), "Debug", false)) {
            mi.log("§8[§cAdvancedBan§8] §cDebug: §7" + msg.toString());
        }
        debugToFile(msg);
    }

    public void debugException(Exception exc){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exc.printStackTrace(pw);

        debug(sw.toString());
    }

    /**
     * Debug.
     *
     * @param ex the ex
     */
    public void debugSqlException(SQLException ex) {
        if (mi.getBoolean(mi.getConfig(), "Debug", false)) {
            debug("§7An error has occurred with the database, the error code is: '" + ex.getErrorCode() + "'");
            debug("§7The state of the sql is: " + ex.getSQLState());
            debug("§7Error message: " + ex.getMessage());
        }
        debugException(ex);
    }

    private void debugToFile(Object msg) {
        File debugFile = new File(mi.getDataFolder(), "logs/latest.log");
        if (!debugFile.exists()) {
            System.out.print("Seems that a problem has occurred while creating the latest.log file in the startup.");
            try {
                debugFile.createNewFile();
            } catch (IOException ex) {
                System.out.print("An error has occurred creating the 'latest.log' file again, check your server.");
                System.out.print("Error message" + ex.getMessage());
            }
        } else {
            logManager.checkLastLog(false);
        }
        try {
            FileUtils.writeStringToFile(debugFile, "[" + new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()) + "] " + ChatColor.stripColor(msg.toString()) + "\n", Charsets.UTF_8, true);
        } catch (IOException ex) {
            System.out.print("An error has occurred writing to 'latest.log' file.");
            System.out.print(ex.getMessage());
        }
    }

    /**
     * Gets gson.
     *
     * @return the gson
     */
    public Gson getGson() {
        return gson;
    }
}
