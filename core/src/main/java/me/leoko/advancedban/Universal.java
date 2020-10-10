package me.leoko.advancedban;

import me.leoko.advancedban.manager.DatabaseManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.manager.UpdateManager;
import me.leoko.advancedban.utils.abstraction.EventHooks;
import me.leoko.advancedban.utils.abstraction.Logger;
import me.leoko.advancedban.utils.abstraction.YamlFile;
import me.leoko.advancedban.utils.commands.Command;
import me.leoko.advancedban.utils.tabcompletion.TabCompleter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


/**
 * This is the server independent entry point of the plugin.
 */
public class Universal {

    private static Universal instance = null;
    private String version;
    private File dataFolder;
    private String serverImplementation;
    private YamlFile config;
    private YamlFile messageFile;
    private YamlFile layoutFile;
    private Logger logger;

    public static Universal getInstance() {
        return instance == null ? instance = new Universal() : instance;
    }

    public void setup(String version, File dataFolder, String serverImplementation, YamlFile config, YamlFile messageFile,
                      YamlFile layoutFile, Logger logger, BiConsumer<String, TabCompleter> commandRegistry, Consumer<EventHooks> eventRegistry) {
        this.version = version;
        this.dataFolder = dataFolder;
        this.serverImplementation = serverImplementation;
        this.config = config;
        this.messageFile = messageFile;
        this.layoutFile = layoutFile;
        this.logger = logger;

        UpdateManager.get().setup();
        UUIDManager.get().setup();

        try {
            DatabaseManager.get().setup(config.getBoolean("UseMySQL", false));
        } catch (Exception ex) {
            logger.log("Failed enabling database-manager...");
            logger.debugException(ex);
        }

//        mi.setupMetrics(); TODO push to server
        PunishmentManager.get().setup();

        eventRegistry.accept(new EventHooks());

        for (Command command : Command.values()) {
            for (String commandName : command.getNames()) {
                commandRegistry.accept(commandName, command.getTabCompleter());
            }
        }

        printStartupMessage();
    }

    private void printStartupMessage() {
        String upt = "You have the newest version";
        String response = getRemoteVersion();
        if (response == null) {
            upt = "Failed to check for updates :(";
        } else if ((!version.startsWith(response))) {
            upt = "There is a new version available! [" + response + "]";
        }

        if (config.getBoolean("DetailedEnableMessage", true)) {
            logger.directLog("\n \n&8[]=====[&7Enabling AdvancedBan&8]=====[]"
                    + "\n&8| &cInformation:"
                    + "\n&8|   &cName: &7AdvancedBan"
                    + "\n&8|   &cDeveloper: &7Leoko"
                    + "\n&8|   &cVersion: &7" + version
                    + "\n&8|   &cStorage: &7" + (DatabaseManager.get().isUseMySQL() ? "MySQL (external)" : "HSQLDB (local)")
                    + "\n&8| &cSupport:"
                    + "\n&8|   &cGithub: &7https://github.com/DevLeoko/AdvancedBan/issues"
                    + "\n&8|   &cDiscord: &7https://discord.gg/ycDG6rS"
                    + "\n&8| &cTwitter: &7@LeokoGar"
                    + "\n&8| &cUpdate:"
                    + "\n&8|   &7" + upt
                    + "\n&8[]================================[]&r\n ");
        } else {
            logger.log("&cEnabling AdvancedBan on Version &7" + version);
            logger.log("&cCoded by &7Leoko &8| &7Twitter: @LeokoGar");
        }
    }


    /**
     * Shutdown.
     */
    public void shutdown() {
        DatabaseManager.get().shutdown();

        if (config.getBoolean("DetailedDisableMessage", true)) {
            logger.log("\n \n&8[]=====[&7Disabling AdvancedBan&8]=====[]"
                    + "\n&8| &cInformation:"
                    + "\n&8|   &cName: &7AdvancedBan"
                    + "\n&8|   &cDeveloper: &7Leoko"
                    + "\n&8|   &cVersion: &7" + version
                    + "\n&8|   &cStorage: &7" + (DatabaseManager.get().isUseMySQL() ? "MySQL (external)" : "HSQLDB (local)")
                    + "\n&8| &cSupport:"
                    + "\n&8|   &cGithub: &7https://github.com/DevLeoko/AdvancedBan/issues"
                    + "\n&8|   &cDiscord: &7https://discord.gg/ycDG6rS"
                    + "\n&8| &cTwitter: &7@LeokoGar"
                    + "\n&8[]================================[]&r\n ");
        } else {
            logger.log("&cDisabling AdvancedBan on Version &7" + version);
            logger.log("&cCoded by Leoko &8| &7Twitter: @LeokoGar");
        }
    }

    private String getRemoteVersion() {
        String response = null;
        try {
            URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=8695");
            Scanner s = new Scanner(url.openStream());
            if (s.hasNext()) {
                response = s.next();
                s.close();
            }
        } catch (IOException exc) {
            logger.debug("!! Failed to get remote version");
        }
        return response;
    }

    public YamlFile getConfig() {
        return config;
    }

    //    public boolean isMuteCommand(String cmd) { //TODO
//        return isMuteCommand(cmd, getMethods().getStringList(getMethods().getConfig(), "MuteCommands"));
//    }


//    boolean isMuteCommand(String cmd, List<String> muteCommands) { TODO
//        String[] words = cmd.split(" ");
//        // Handle commands with colons
//        if (words[0].indexOf(':') != -1) {
//            words[0] = words[0].split(":", 2)[1];
//        }
//        for (String muteCommand : muteCommands) {
//            if (muteCommandMatches(words, muteCommand)) {
//                return true;
//            }
//        }
//        return false;
//    }

//    boolean muteCommandMatches(String[] commandWords, String muteCommand) { TODO
//        // Basic equality check
//        if (commandWords[0].equalsIgnoreCase(muteCommand)) {
//            return true;
//        }
//        // Advanced equality check
//        // Essentially a case-insensitive "startsWith" for arrays
//        if (muteCommand.indexOf(' ') != -1) {
//            String[] muteCommandWords = muteCommand.split(" ");
//            if (muteCommandWords.length > commandWords.length) {
//                return false;
//            }
//            for (int n = 0; n < muteCommandWords.length; n++) {
//                if (!muteCommandWords[n].equalsIgnoreCase(commandWords[n])) {
//                    return false;
//                }
//            }
//            return true;
//        }
//        return false;
//    }


//    public boolean isExemptPlayer(String name) { TODO
//        List<String> exempt = getMethods().getStringList(getMethods().getConfig(), "ExemptPlayers");
//        if (exempt != null) {
//            for (String str : exempt) {
//                if (name.equalsIgnoreCase(str)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    /**
     * Whether the dev should be announced when he joins the server c:
     *
     * @return the boolean
     */
    public boolean broadcastLeoko() {
        return !new File(dataFolder, "STOP-BROADCAST").exists();
    }
}
