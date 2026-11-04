package me.leoko.advancedban.manager;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

        if (!mi.contains(mi.getMessages(), "UnNote.Usage")) {
            try {
                addMessage("Check:", "  Note: \"&cNotes &8» &7%COUNT%\"", 1);
                FileUtils.writeLines(new File(mi.getDataFolder(), "Messages.yml"), "UTF8", Arrays.asList(
                        "",
                        "# Automatically added by v2.2.1 update process",
                        "UnNote:",
                        "  Usage: \"&cUsage &8» &7&o/unnote [ID] or /unnote clear [Name]\"",
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
                List<String> lines = FileUtils.readLines(file, StandardCharsets.UTF_8);
                int index = lines.indexOf("Check:");
                lines.add(index + 1, "  MuteReason: \"  &cReason &8\\xbb &7%REASON%\"");
                FileUtils.writeLines(file, "UTF8", lines);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        if (!mi.contains(mi.getMessages(), "Check.BanReason")) {
            try {
                File file = new File(mi.getDataFolder(), "Messages.yml");
                List<String> lines = FileUtils.readLines(file, StandardCharsets.UTF_8);
                int index = lines.indexOf("Check:");
                lines.add(index + 1, "  BanReason: \"  &cReason &8\\xbb &7%REASON%\"");
                FileUtils.writeLines(file, "UTF8", lines);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (!mi.contains(mi.getMessages(), "Tempipban")) {
            try {
                FileUtils.writeLines(new File(mi.getDataFolder(), "Messages.yml"), "UTF8", Arrays.asList(
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
        if (!mi.contains(mi.getMessages(), "Punish.Usage")) {
            try {
                FileUtils.writeLines(new File(mi.getDataFolder(), "Messages.yml"), "UTF8", Arrays.asList(
                        "",
                        "# Automatically added by v3.0.0 update process",
                        "Punish:",
                        "  Usage: \"&cUsage &8» &7&o/punish (-s) [Name] [Offence] <Details>\"",
                        "  Done: \"&c&o%NAME% &7was punished for &e&o%OFFENCE% &8(&7step %STEP%/%STEPS%: %ACTION%&8)\"",
                        "  NotFound: \"&cThere is no offence called %NAME%! &7Available: %OFFENCES%\"",
                        "  InvalidStep: \"&cThe step '%STEP%' of the offence %OFFENCE% is invalid, please check the Layouts.yml\"",
                        "",
                        "Alts:",
                        "  Usage: \"&cUsage &8» &7&o/alts [Name/IP]\"",
                        "  Disabled: \"&cAlt detection is disabled in the config.yml\"",
                        "  NoEntries: \"&7No other accounts found for &e&o%NAME%\"",
                        "  Header: \"&7Accounts sharing an IP with &e&o%NAME%&7:\"",
                        "  Entry: \"&8- &7%ALT% &8(%STATUS%&8) &7last seen %DATE%\"",
                        "  Banned: \"&cbanned\"",
                        "  Muted: \"&emuted\"",
                        "  Clean: \"&aclean\"",
                        "",
                        "AltDetection:",
                        "  Notification:",
                        "  - \"%PREFIX% &c&o%NAME% &7shares an IP with the banned account &c&o%ALT%\"",
                        "  - \"&7Ban reason: &o%REASON%\""
                ), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!mi.contains(mi.getLayouts(), "Ladder")) {
            try {
                FileUtils.writeLines(new File(mi.getDataFolder(), "Layouts.yml"), "UTF8", Arrays.asList(
                        "",
                        "# Ladders are used by \"/punish Leoko Hacking\". Every time a player gets punished for",
                        "# the same offence the next step is used, and the last one stays after reaching it.",
                        "# Possible steps: ban, tempban X, mute, tempmute X, warn, tempwarn X, kick and note",
                        "# with X being a time like 30m, 2h, 1d, 1w or 1mo. IP bans can not be used here.",
                        "# The steps ignore the ab.*.dur.* limits, only the permission ab.punish is required.",
                        "# The reason can also use a message layout like \"@ExampleLayout\"",
                        "Ladder:",
                        "  Hacking:",
                        "    Reason: \"Using a hacked client\"",
                        "    Steps:",
                        "    - 'tempban 7d'",
                        "    - 'tempban 30d'",
                        "    - 'ban'",
                        "  Spam:",
                        "    Reason: \"Spamming\"",
                        "    Steps:",
                        "    - 'warn'",
                        "    - 'tempmute 1h'",
                        "    - 'tempmute 1d'",
                        "    - 'mute'"
                ), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            File file = new File(mi.getDataFolder(), "config.yml");
            List<String> lines = FileUtils.readLines(file, StandardCharsets.UTF_8);
            migrateUuidApis(lines);
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
            if (!mi.contains(mi.getConfig(), "AltDetection")) {
                lines.addAll(Arrays.asList("",
                        "# Remembers which accounts have been using which IP to find alternative accounts.",
                        "# This is used by /alts and alerts players with the permission ab.alts.notify if",
                        "# someone joins who has been using the same IP as a banned account.",
                        "AltDetection:",
                        "  Enabled: true",
                        "  # After how many days of not being used a login gets forgotten",
                        "  Retention Days: 90",
                        "  # IPs used by more accounts than this will not be taken into account, as they",
                        "  # say nothing about who is behind them. (e.g. schools or a misconfigured proxy)",
                        "  Shared IP Limit: 10",
                        "  # These IPs are never remembered",
                        "  IgnoredIPs:",
                        "  - '127.0.0.1'",
                        "  - '::1'",
                        "  # Also ban accounts which are joining from an IP used by a banned account. They get",
                        "  # banned for as long as the other account. Be careful: people sharing an internet",
                        "  # connection, like siblings, would get banned as well!",
                        "  # An account only gets banned once like this, so that it can be unbanned with /unban.",
                        "  # This does nothing on servers in offline mode, as anyone could use the name of someone",
                        "  # else there. That includes servers behind a proxy: use AdvancedBan on the proxy for this.",
                        "  # Excluded are players in ExemptPlayers and, if a permission plugin can tell for players",
                        "  # who are not online (Vault, LuckPerms or CloudNet-CloudPerms), the permission ab.ban.exempt.",
                        "  AutoBan:",
                        "    Enabled: false",
                        "    Reason: \"Ban evasion (alt of %ALT%)\""));
            }
            FileUtils.writeLines(file, "UTF8", lines);
        } catch (IOException exc) {
            exc.printStackTrace();
        }

        // Everything that was just added has to be loaded, otherwise it would be missing until the next restart
        mi.loadFiles();
    }

    private void migrateUuidApis(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            // Mojang no longer supports looking up names at a certain point in time
            lines.set(i, line.replace("/users/profiles/minecraft/%NAME%?at=%TIMESTAMP%", "/users/profiles/minecraft/%NAME%"));

            // This API has been shut down
            if (line.contains("https://us.mc-api.net/v3/uuid/%NAME%")) {
                lines.set(i, line.replace("https://us.mc-api.net/v3/uuid/%NAME%", "https://api.minetools.eu/uuid/%NAME%"));
                if (i + 1 < lines.size() && lines.get(i + 1).trim().equals("Key: \"uuid\"")) {
                    lines.set(i + 1, lines.get(i + 1).replace("\"uuid\"", "\"id\""));
                }
            }
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
