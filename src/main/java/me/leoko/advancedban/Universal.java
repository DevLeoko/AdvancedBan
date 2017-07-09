package me.leoko.advancedban;

import me.leoko.advancedban.manager.MySQLManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.utils.Punishment;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Leoko @ dev.skamps.eu on 23.07.2016.
 */
public class Universal {
    private static Universal instance = null;
    private final Map<String, String> ips = new HashMap<>();
    private MethodInterface mi;
    private MySQLManager mysql;
    private boolean useMySQL = false;

    public static Universal get() {
        return instance == null ? instance = new Universal() : instance;
    }

    //TODO Main-Points
    // -> Improve performance by adding player-data
    // -> Offline-Exempt
    // -> DoubleIP

    public void setup(MethodInterface mi) {
        this.mi = mi;
        mi.loadFiles();

        if (useMySQL = mi.getBoolean(mi.getConfig(), "UseMySQL", false)) {
            mysql = new MySQLManager(new File(mi.getDataFolder(), "MySQL.yml"), true, 10);
            useMySQL = !mysql.isFailed();
        }

        PunishmentManager.get().setup();

        mi.setCommandExecutor("advancedban");
        mi.setCommandExecutor("advancedban");
        mi.setCommandExecutor("ban");
        mi.setCommandExecutor("tempban");
        mi.setCommandExecutor("ipban");
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
        String response = getFromURL("http://dev.skamps.eu/api/abVer.txt");
        if (response == null) {
            upt = "Failed to check for updates :(";
        } else if (!response.equalsIgnoreCase(mi.getVersion())) {
            upt = "There is a new version available! [" + response + "]";
        }

        getMethods().scheduleAsyncRep(() -> {
            try {
                new URL("http://dev.skamps.eu/api/stats.php?player=" + getMethods().getOnlinePlayers().length).openConnection().connect();
            } catch (IOException e) {
                System.out.println("Failed to connect to stats-server");
            }
        }, 20 * 60 * 15, 20 * 60 * 15);

        if (mi.getBoolean(mi.getConfig(), "DetailedEnableMessage", true)) {
            System.out.println("\n \n[]=====[Enabling AdvancedBan]=====[]"
                    + "\n| Information:"
                    + "\n|   Name: AdvancedBan"
                    + "\n|   Developer: Leoko"
                    + "\n|   Version: " + mi.getVersion()
                    + "\n|   MySQL: " + useMySQL
                    + "\n| Support:"
                    + "\n|   Skype: Leoko33"
                    + "\n|   Mail: Leoko4433@gmail.com"
                    + "\n| Update:"
                    + "\n|   " + upt
                    + "\n[]================================[]\n ");
        } else {
            System.out.println("Enabling AdvancedBan on Version " + mi.getVersion());
            System.out.println("Coded by Leoko | Web: dev.skamps.eu");
        }
    }

    public void shutdown() {
        if (mi.getBoolean(mi.getConfig(), "DetailedDisableMessage", true)) {
            System.out.println("\n \n[]=====[Disabling AdvancedBan]=====[]"
                    + "\n| Information:"
                    + "\n|   Name: AdvancedBan"
                    + "\n|   Developer: Leoko"
                    + "\n|   Version: " + getMethods().getVersion()
                    + "\n|   MySQL: " + useMySQL
                    + "\n| Support:"
                    + "\n|   Skype: Leoko33"
                    + "\n|   Mail: Leoko4433@gmail.com"
                    + "\n[]================================[]\n ");
        } else {
            System.out.println("Disabling AdvancedBan on Version " + getMethods().getVersion());
            System.out.println("Coded by Leoko | Web: dev.skamps.eu");
        }
    }

    public MySQLManager getMysql() {
        return mysql;
    }

    public boolean isUseMySQL() {
        return useMySQL;
    }

    public Map<String, String> getIps() {
        return ips;
    }

    public MethodInterface getMethods() {
        return mi;
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
            System.out.println("AdvancedBan <> !! Failed to connect to URL: " + surl);
        }
        return response;
    }

    public boolean isMuteCommand(String cmd) {
        cmd = cmd.contains(":") ? cmd.split(":", 2)[1] : cmd;
        for (String str : getMethods().getStringList(getMethods().getConfig(), "MuteCommands"))
            if (cmd.equalsIgnoreCase(str)) return true;
        return false;
    }


    public boolean isExemptPlayer(String name) {
        List<String> exempt = getMethods().getStringList(getMethods().getConfig(), "ExemptPlayers");
        if (exempt != null) for (String str : exempt) if (name.equalsIgnoreCase(str)) return true;
        return false;
    }

    public boolean broadcastLeoko() {
        File readme = new File(getMethods().getDataFolder(), "readme.txt");
        if (!readme.exists()) return true;
        try {
            if (Files.readAllLines(Paths.get(readme.getPath()), Charset.defaultCharset()).get(0).equalsIgnoreCase("I don't want that there will be any message when the dev of this plugin joins the server! I want this even though the plugin is 100% free and the join-message is the only reward for the Dev :("))
                return false;
        } catch (IOException ignore) {
        }
        return true;
    }

    public String callConnection(String name, String ip) {
        name = name.toLowerCase();
        String uuid = UUIDManager.get().getUUID(name);
        if (uuid == null) return "[AdvancedBan] Failed to fetch your UUID";
        Punishment pt = PunishmentManager.get().getBan(uuid);
        if (pt == null) pt = PunishmentManager.get().getBan(ip);
        if (pt != null) {
            return pt.getLayoutBSN();
        }

        if (Universal.get().getIps().containsKey(name)) {
            Universal.get().getIps().remove(name);
        }
        Universal.get().getIps().put(name, ip);
        return null;
    }
}
