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
 * Created by Leo on 07.08.2017.
 */
public class UpdateManager {
    private static UpdateManager instance = null;

    public static UpdateManager get() {
        return instance == null ? instance = new UpdateManager() : instance;
    }

    public void setup(){
        MethodInterface mi = Universal.get().getMethods();
        if(!mi.contains(mi.getMessages(), "Tempipban")){
            try {
                    FileUtils.writeLines( new File(mi.getDataFolder(), "Messages.yml"), Arrays.asList(
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


        if(!mi.contains(mi.getConfig(), "EnableAllPermissionNodes")){
            try {
                File file = new File(mi.getDataFolder(), "config.yml");
                List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());

                lines.remove("  # Disable for cracked servers");

                int indexOf = lines.indexOf("UUID-Fetcher:");
                if(indexOf != -1){
                    lines.addAll(indexOf+1, Arrays.asList(
                            "  # If dynamic it set to true it will override the 'enabled' and 'intern' settings",
                            "  # and automatically detect the best possible uuid fetcher settings for your server.",
                            "  # Our recommendation: don't set dynamic to false if you don't have any problems.",
                            "  Dynamic: true"));
                }

                lines.addAll(Arrays.asList( "",
                        "# This is useful for bungeecord servers or server with permission systems which do not support *-Perms",
                        "# So if you enable this you can use ab.all instead of ab.* or ab.ban.all instead of ab.ban.*",
                        "# This does not work with negative permissions! e.g. -ab.all would not block all commands for that user.",
                        "EnableAllPermissionNodes: false"));

                FileUtils.writeLines(file, lines);
            }catch (IOException exc){
                exc.printStackTrace();
            }
        }
    }
}
