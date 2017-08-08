package me.leoko.advancedban;

import me.leoko.advancedban.manager.DatabaseManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.manager.UpdateManager;
import me.leoko.advancedban.utils.InterimData;
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

    public static Universal get() {
        return instance == null ? instance = new Universal() : instance;
    }

    public void setup(MethodInterface mi) {
        this.mi = mi;
        mi.loadFiles();

        UpdateManager.get().setup();

        try{
            DatabaseManager.get().setup(mi.getBoolean(mi.getConfig(), "UseMySQL", false));
        }catch (Exception exc){
            exc.printStackTrace();
            System.out.println("Failed enabling database-manager...");
        }

        mi.setupMetrics();
        PunishmentManager.get().setup();

        mi.setCommandExecutor("advancedban");
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
        String response = getFromURL("http://dev.skamps.eu/api/abVer.txt");
        if (response == null) {
            upt = "Failed to check for updates :(";
        } else if (!response.equalsIgnoreCase(mi.getVersion())) {
            upt = "There is a new version available! [" + response + "]";
        }

        if (mi.getBoolean(mi.getConfig(), "DetailedEnableMessage", true)) {
            System.out.println("\n \n[]=====[Enabling AdvancedBan]=====[]"
                    + "\n| Information:"
                    + "\n|   Name: AdvancedBan"
                    + "\n|   Developer: Leoko"
                    + "\n|   Version: " + mi.getVersion()
                    + "\n|   Storage: " + (DatabaseManager.get().isUseMySQL() ? "MySQL (external)" : "HSQLDB (local)")
                    + "\n| Support:"
                    + "\n|   Skype: Leoko33"
                    + "\n|   Mail: Leoko4433@gmail.com"
                    + "\n| Update:"
                    + "\n|   " + upt
                    + "\n[]================================[]\n ");
        } else {
            System.out.println("Enabling AdvancedBan on Version " + mi.getVersion());
            System.out.println("Coded by Leoko | Web: Skamps.eu");
        }
    }

    public void shutdown() {
        DatabaseManager.get().shutdown();

        if (mi.getBoolean(mi.getConfig(), "DetailedDisableMessage", true)) {
            System.out.println("\n \n[]=====[Disabling AdvancedBan]=====[]"
                    + "\n| Information:"
                    + "\n|   Name: AdvancedBan"
                    + "\n|   Developer: Leoko"
                    + "\n|   Version: " + getMethods().getVersion()
                    + "\n|   Storage: " + (DatabaseManager.get().isUseMySQL() ? "MySQL (external)" : "HSQLDB (local)")
                    + "\n| Support:"
                    + "\n|   Skype: Leoko33"
                    + "\n|   Mail: Leoko4433@gmail.com"
                    + "\n[]================================[]\n ");
        } else {
            System.out.println("Disabling AdvancedBan on Version " + getMethods().getVersion());
            System.out.println("Coded by Leoko | Web: Skamps.eu");
        }
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

    public String callConnection(String name, String ip){
        name = name.toLowerCase();
        String uuid = UUIDManager.get().getUUID(name);
        if (uuid == null) {
            return "[AdvancedBan] Failed to fetch your UUID";
        }

        ips.remove(name);
        ips.put(name, ip);

        InterimData interimData = PunishmentManager.get().load(name, uuid, ip);
        Punishment pt = interimData.getBan();

        if(pt == null){
            interimData.accept();
            return null;
        }

        return pt.getLayoutBSN();
    }
}
