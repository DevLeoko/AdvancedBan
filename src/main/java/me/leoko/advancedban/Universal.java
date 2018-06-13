package me.leoko.advancedban;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import me.leoko.advancedban.bungee.BungeeMethods;
import me.leoko.advancedban.manager.DatabaseManager;
import me.leoko.advancedban.manager.LogManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.manager.UpdateManager;
import me.leoko.advancedban.utils.InterimData;
import me.leoko.advancedban.utils.Punishment;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.io.FileUtils;

/**
 * Created by Leoko @ dev.skamps.eu on 23.07.2016.
 */
public class Universal {

    private static Universal instance = null;
    private final Map<String, String> ips = new HashMap<>();
    private MethodInterface mi;
    private LogManager logManager;
    private static boolean redis = false;
    private final Gson gson = new Gson();

    public static Universal get() {
        return instance == null ? instance = new Universal() : instance;
    }

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
                    + "\n&8|   &cContributors: &7Beelzebu&c, &7BrainStone&c, &7ItzSomebody&c, &7BillyGalbreath&c, &7ZombieHDGaming&c, &7dutchy1001&c, &7and many others..."
                    + "\n&8|   &cVersion: &7" + mi.getVersion()
                    + "\n&8|   &cStorage: &7" + (DatabaseManager.get().isUseMySQL() ? "MySQL (external)" : "HSQLDB (local)")
                    + "\n&8| &cSupport:"
                    + "\n&8|   &cSkype: &7Leoko33"
                    + "\n&8|   &cMail: &7Leoko4433@gmail.com"
                    + "\n&8|   &cGithub: &7https://github.com/DevLeoko/AdvancedBan/issues"
                    + "\n&8|   &cDiscord: &7https://discord.gg/ycDG6rS"
                    + "\n&8| &cUpdate:"
                    + "\n&8|   &7" + upt
                    + "\n&8[]================================[]&r\n ");
        } else {
            mi.log("&cEnabling AdvancedBan on Version &7" + mi.getVersion());
            mi.log("&cCoded by &7Leoko &8| &cWeb: &7Skamps.eu");
        }
    }

    public void shutdown() {
        DatabaseManager.get().shutdown();

        if (mi.getBoolean(mi.getConfig(), "DetailedDisableMessage", true)) {
            mi.log("\n \n&8[]=====[&7Disabling AdvancedBan&8]=====[]"
                    + "\n&8| &cInformation:"
                    + "\n&8|   &cName: &7AdvancedBan"
                    + "\n&8|   &cDeveloper: &7Leoko"
                    + "\n&8|   &cContributors: &7Beelzebu&c, &7BrainStone&c, &7ItzSomebody&c, &7BillyGalbreath&c, &7ZombieHDGaming&c, &7dutchy1001&c, &7and many others..."
                    + "\n&8|   &cVersion: &7" + getMethods().getVersion()
                    + "\n&8|   &cStorage: &7" + (DatabaseManager.get().isUseMySQL() ? "MySQL (external)" : "HSQLDB (local)")
                    + "\n&8| &cSupport:"
                    + "\n&8|   &cSkype: &7Leoko33"
                    + "\n&8|   &cMail: &7Leoko4433@gmail.com"
                    + "\n&8|   &cGithub: &7https://github.com/DevLeoko/AdvancedBan/issues"
                    + "\n&8|   &cDiscord: &7https://discord.gg/ycDG6rS"
                    + "\n&8[]================================[]&r\n ");
        } else {
            mi.log("&cDisabling AdvancedBan on Version &7" + getMethods().getVersion());
            mi.log("&cCoded by Leoko &8| &7Web: Skamps.eu");
        }
    }

    public Map<String, String> getIps() {
        return ips;
    }

    public MethodInterface getMethods() {
        return mi;
    }

    public boolean isBungee() {
        return mi instanceof BungeeMethods;
    }

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

    public boolean isMuteCommand(String cmd) {
        cmd = cmd.contains(":") ? cmd.split(":", 2)[1] : cmd;
        for (String str : getMethods().getStringList(getMethods().getConfig(), "MuteCommands")) {
            if (cmd.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

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

    public String callConnection(String name, String ip) {
        name = name.toLowerCase();
        String uuid = UUIDManager.get().getUUID(name);
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

    public void useRedis(boolean use) {
        redis = use;
    }

    public boolean useRedis() {
        return redis;
    }

    public void log(String msg) {
        mi.log("§8[§cAdvancedBan§8] §7" + msg);
        debugToFile(msg);
    }

    public void debug(Object msg) {
        if (mi.getBoolean(mi.getConfig(), "Debug", false)) {
            mi.log("§8[§cAdvancedBan§8] §cDebug: §7" + msg.toString());
        }
        debugToFile(msg);
    }

    public void debug(SQLException ex) {
        if (mi.getBoolean(mi.getConfig(), "Debug", false)) {
            debug("§7An error has ocurred with the database, the error code is: '" + ex.getErrorCode() + "'");
            debug("§7The state of the sql is: " + ex.getSQLState());
            debug("§7Error message: " + ex.getMessage());
        }
    }

    private void debugToFile(Object msg) {
        File debugFile = new File(mi.getDataFolder(), "logs/latest.log");
        if (!debugFile.exists()) {
            System.out.print("Seems that a problem has ocurred while creating the latest.log file in the startup.");
            try {
                debugFile.createNewFile();
            } catch (IOException ex) {
                System.out.print("An error has ocurred creating the 'latest.log' file again, check your server.");
                System.out.print("Error message" + ex.getMessage());
            }
        } else {
            logManager.checkLastLog(false);
        }
        try {
            FileUtils.writeStringToFile(debugFile, "[" + new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()) + "] " + ChatColor.stripColor(msg.toString()) + "\n", Charsets.UTF_8, true);
        } catch (IOException ex) {
            System.out.print("An error has ocurred writing to 'latest.log' file.");
            System.out.print(ex.getMessage());
        }
    }

    public Gson getGson() {
        return gson;
    }
}
