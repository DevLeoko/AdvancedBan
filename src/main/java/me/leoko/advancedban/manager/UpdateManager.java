package me.leoko.advancedban.manager;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

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
    }
}
