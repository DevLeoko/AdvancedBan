package me.leoko.advancedban.manager;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;


/**
 * The Update Manager used to keep config files up to date and migrate them seamlessly to the newest version.
 */
public class UpdateManager {

    private static UpdateManager instance = null;

    /**
     * Get the update manager.
     *
     * @return the update manager instance
     */
    public static synchronized UpdateManager get() {
        return instance == null ? instance = new UpdateManager() : instance;
    }

    /**
     * Initially checks which configuration options from the newest version are missing and tries to add them
     * without altering any old configuration settings.
     */
    public void setup() {
        MethodInterface mi = Universal.get().getMethods();

        if (mi.isUnitTesting()) return;

        if(!mi.contains(mi.getConfig(), "FullHistory")){
            try {
                FileUtils.writeLines(new File(mi.getDataFolder(), "config.yml"), "UTF8", Arrays.asList(
                        "# These are the Punishment types that show up when running /history on a player",
                        "FullHistory:",
                        "  - \"BAN\"",
                        "  - \"IP_BAN\"",
                        "  - \"MUTE\"",
                        "  - \"WARNING\"",
                        "  - \"KICK\"",
                        "  - \"NOTE\""
                ), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!mi.contains(mi.getMessages(), "UnNote.Usage")) {
            try {
                addMessage("Check:", "  Note: \"&cNotes &8» &7%COUNT%\"", 1);
                FileUtils.writeLines(new File(mi.getDataFolder(), "Messages.yml"), "UTF8", Arrays.asList(
                        "",
                        "# Automatically added by v2.2.1 update process",
                        "UnNote:",
                        "  Usage: \"&cUsage &8» &7&o/unwarn [ID] or /unnote clear [Name]\"",
                        "  NotFound: \"&cCould not find note #%ID%\"",
                        "  Done: \"&7Note &a&o#%ID% &7was successfully deleted!\"",
                        "  Notification: \"&e&o%OPERATOR% &7unnoted &c&o%NAME%\"",
                        "  Clear:",
                        "    Empty: \"&c&o%NAME% &7has no notes!\"",
                        "    Done: \"&7Cleared &a&o%COUNT% &7notes\"",
                        "",
                        "Note:",
                        "  Usage: \"&cUsage &8» &7&o/note [Name] [Reason]\"",
                        "  Done: \"&c&o%NAME% &7was successfully noted!\"",
                        "  Exempt: \"&7You are not able to note &c&o%NAME%\"",
                        "  Notification:",
                        "    - \"&c&o%NAME% &7got noted by &e&o%OPERATOR%\"",
                        "    - \"&7For the reason &o%REASON%\"",
                        "",
                        "Notes:",
                        "  Usage: \"&cUsage &8» &7&o/notes [Name] <Page> &cor &7&o/notes <Page>\"",
                        "  OutOfIndex: \"&cThere is no page %PAGE%!\"",
                        "  NoEntries: \"&c&o%NAME% has no notes yet\"",
                        "  Header:",
                        "    - \"&7\"",
                        "    - \"%PREFIX% &7Notes for %NAME%:\"",
                        "    - \"&7\"",
                        "  Entry:",
                        "    - \"&7%DATE% &8| &7By &o%OPERATOR% &7(&c#%ID%&7)\"",
                        "    - \"&8> &e%REASON%\"",
                        "    - \"&7\"",
                        "  Footer: \"&7Page &e&o%CURRENT_PAGE% &7of &e&o%TOTAL_PAGES% &8| &7Notes: &e&o%COUNT%\"",
                        "  PageFooter: \"&7Use &e&o/notes %NAME% %NEXT_PAGE% &7to see the next page\"",
                        "",
                        "NotesOwn:",
                        "  OutOfIndex: \"&cThere is no page %PAGE%!\"",
                        "  NoEntries: \"&c&oYou have no notes yet\"",
                        "  Header:",
                        "    - \"&7\"",
                        "    - \"%PREFIX% &7Your notes:\"",
                        "    - \"&7\"",
                        "  Entry:",
                        "    - \"&7%DATE% &8| &7By &o%OPERATOR% &7(&c#%ID%&7)\"",
                        "    - \"&8> &e%REASON%\"",
                        "    - \"&7\"",
                        "  Footer: \"&7Page &e&o%CURRENT_PAGE% &7of &e&o%TOTAL_PAGES% &8| &7Notes: &e&o%COUNT%\"",
                        "  PageFooter: \"&7Use &e&o/notes %NEXT_PAGE% &7to see the next page\""
                ), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!mi.contains(mi.getMessages(), "WarnsOwn")) {
            addMessage("ChangeReason:", "", 0);
            addMessage("ChangeReason:", "WarnsOwn:", -1);
            addMessage("ChangeReason:", "  OutOfIndex: \"&cThere is no page %PAGE%!\"", -1);
            addMessage("ChangeReason:", "  NoEntries: \"&c&oYou have no warnings yet\"", -1);
            addMessage("ChangeReason:", "  Header:", -1);
            addMessage("ChangeReason:", "    - \"%PREFIX% &7Your warnings:\"", -1);
            addMessage("ChangeReason:", "    - \"&e&oDuration &8| &7&oWarned by\"", -1);
            addMessage("ChangeReason:", "    - \"&c&o#ID &8> &7&oReason\"", -1);
            addMessage("ChangeReason:", "    - \"&7\"", -1);
            addMessage("ChangeReason:", "  Entry:", -1);
            addMessage("ChangeReason:", "    - \"&8[&e%DATE%&8]\"", -1);
            addMessage("ChangeReason:", "    - \"&e%DURATION% &8| &7%OPERATOR%\"", -1);
            addMessage("ChangeReason:", "    - \"&c&l#%ID% &8> &7&o%REASON%\"", -1);
            addMessage("ChangeReason:", "    - \"&7\"", -1);
            addMessage("ChangeReason:", "  Footer: \"&7Page &e&o%CURRENT_PAGE% &7of &e&o%TOTAL_PAGES% &8| &7Active warnings: &e&o%COUNT%\"", -1);
            addMessage("ChangeReason:", "  PageFooter: \"&7Use &e&o/warns %NEXT_PAGE% &7to see the next page\"", -1);
        }

        if (!mi.contains(mi.getMessages(), "UnBan.Notification")) {
            addMessage("UnBan:", "  Notification: \"&e&o%OPERATOR% &7unbanned &c&o%NAME%\"", 1);
            addMessage("UnMute:", "  Notification: \"&e&o%OPERATOR% &7unmuted &c&o%NAME%\"", 1);
            addMessage("UnWarn:", "  Notification: \"&e&o%OPERATOR% &7unwarned &c&o%NAME%\"", 1);
        }

        if (!mi.contains(mi.getMessages(), "Check.MuteReason")) {
            try {
                File file = new File(mi.getDataFolder(), "Messages.yml");
                List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());
                int index = lines.indexOf("Check:");
                lines.add(index + 1, "  MuteReason: \"  &cReason &8\\xbb &7%REASON%\"");
                FileUtils.writeLines(file, lines);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        if (!mi.contains(mi.getMessages(), "Check.BanReason")) {
            try {
                File file = new File(mi.getDataFolder(), "Messages.yml");
                List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());
                int index = lines.indexOf("Check:");
                lines.add(index + 1, "  BanReason: \"  &cReason &8\\xbb &7%REASON%\"");
                FileUtils.writeLines(file, lines);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (!mi.contains(mi.getMessages(), "Tempipban")) {
            try {
                FileUtils.writeLines(new File(mi.getDataFolder(), "Messages.yml"), Arrays.asList(
                        "",
                        "Tempipban:",
                        "  Usage: \"&cUsage &8\\xbb &7&o/tempipban [Name/IP] [Xmo/Xd/Xh/Xm/Xs/#TimeLayout] [Reason/@Layout]\"",
                        "  MaxDuration: \"&cYou are not able to ban more than %MAX%sec\"",
                        "  Layout:",
                        "  - '%PREFIX% &7Temporarily banned'",
                        "  - '&7'",
                        "  - '&7'",
                        "  - \"&cReason &8\\xbb &7%REASON%\"",
                        "  - \"&cDuration &8\\xbb &7%DURATION%\"",
                        "  - '&7'",
                        "  - '&8Unban application in TS or forum'",
                        "  - \"&eTS-Ip &8\\xbb &c&ncoming soon\"",
                        "  - \"&eForum &8\\xbb &c&ncoming soon\"",
                        "  Notification:",
                        "  - \"&c&o%NAME% &7got banned by &e&o%OPERATOR%\"",
                        "  - \"&7For the reason &o%REASON%\"",
                        "  - \"&7&oThis player got banned for &e&o%DURATION%\"",
                        "",
                        "ChangeReason:",
                        "  Usage: \"&cUsage &8\\xbb &7&o/change-reason [ID or ban/mute USER] [New reason]\"",
                        "  Done: \"&7Punishment &a&o#%ID% &7has successfully been updated!\"",
                        "  NotFound: \"&cSorry we have not been able to find this punishment\""), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            File file = new File(mi.getDataFolder(), "config.yml");
            List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());
            if (!mi.contains(mi.getConfig(), "EnableAllPermissionNodes")) {

                lines.remove("  # Disable for cracked servers");

                int indexOf = lines.indexOf("UUID-Fetcher:");
                if (indexOf != -1) {
                    lines.addAll(indexOf + 1, Arrays.asList(
                            "  # If dynamic it set to true it will override the 'enabled' and 'intern' settings",
                            "  # and automatically detect the best possible uuid fetcher settings for your server.",
                            "  # Our recommendation: don't set dynamic to false if you don't have any problems.",
                            "  Dynamic: true"));
                }

                lines.addAll(Arrays.asList("",
                        "# This is useful for bungeecord servers or server with permission systems which do not support *-Perms",
                        "# So if you enable this you can use ab.all instead of ab.* or ab.ban.all instead of ab.ban.*",
                        "# This does not work with negative permissions! e.g. -ab.all would not block all commands for that user.",
                        "EnableAllPermissionNodes: false"));
            }
            if (!mi.contains(mi.getConfig(), "Debug")) {
                lines.addAll(Arrays.asList(
                        "",
                        "# With this active will show more information in the console, such as errors, if",
                        "# the plugin works correctly is not recommended to activate it since it is",
                        "# designed to find bugs.",
                        "Debug: false"));
            }
            if (mi.contains(mi.getConfig(), "Logs Purge Days")) {
                lines.removeAll(Arrays.asList(
                        "",
                        "# This is the amount of days that we should keep plugin logs in the plugins/AdvancedBan/logs folder.",
                        "# By default is set to 10 days.",
                        "Logs Purge Days: 10"
                ));
            }
            if (!mi.contains(mi.getConfig(), "Log Purge Days")) {
                lines.addAll(Arrays.asList(
                        "",
                        "# This is the amount of days that we should keep plugin logs in the plugins/AdvancedBan/logs folder.",
                        "# By default is set to 10 days.",
                        "Log Purge Days: 10"
                ));
            }
            if (!mi.contains(mi.getConfig(), "Disable Prefix")) {
                lines.addAll(Arrays.asList(
                        "",
                        "# Removes the prefix of the plugin in every message.",
                        "Disable Prefix: false"
                ));
            }
            if (!mi.contains(mi.getConfig(), "Friendly Register Commands")) {
                lines.addAll(Arrays.asList("",
                        "# Register commands in a more friendly manner",
                        "# Off by default, so AdvancedBan can override /ban from other plugins",
                        "# This is a Bukkit-specific option. It has no meaning on BungeeCord",
                        "Friendly Register Commands: false"));
            }
            FileUtils.writeLines(file, lines);
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    private void addMessage(String search, String insert, int indexOffset) {
        try {
            File file = new File(Universal.get().getMethods().getDataFolder(), "Messages.yml");
            List<String> lines = FileUtils.readLines(file, "UTF8");
            int index = lines.indexOf(search);
            lines.add(index + indexOffset, insert);
            FileUtils.writeLines(file, "UTF8", lines);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
