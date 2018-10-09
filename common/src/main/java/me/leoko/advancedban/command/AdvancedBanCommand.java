package me.leoko.advancedban.command;

import me.leoko.advancedban.AdvancedBanCommandSender;

import java.io.IOException;

public class AdvancedBanCommand extends AbstractCommand {
    public AdvancedBanCommand() {
        super("advancedban", "ab.info", "advancedban", "ab");
    }

    @Override
    public boolean onCommand(AdvancedBanCommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§8§l§m-=====§r §c§lAdvancedBan §8§l§m=====-§r ");
            sender.sendMessage("  §cDev §8• §7Leoko");
            sender.sendMessage("  §cVersion §8• §7" + sender.getAdvancedBan().getVersion());
            sender.sendMessage("  §cUUID Fetcher Mode §8• §7" + sender.getAdvancedBan().getUuidManager().getMode());
            sender.sendMessage("  §cDebugging §8• §7" + Boolean.toString(sender.getAdvancedBan().getConfiguration().isDebug()));
        } else if (args[0].equalsIgnoreCase("help")) {
            if (!sender.hasPermission("ab.help")) {
                sendPermissionMessage(sender);
                return true;
            }
            sender.sendMessage("§8");
            sender.sendMessage("§c§lAdvancedBan §7Command-Help");
            sender.sendMessage("§8");
            sender.sendMessage("§c/ban [Name] [Reason/@Layout]");
            sender.sendMessage("§8» §7Ban a user permanently");
            sender.sendMessage("§c/banip [Name/IP] [Reason/@Layout]");
            sender.sendMessage("§8» §7Ban a user by IP");
            sender.sendMessage("§c/tempban [Name] [Xmo/Xd/Xh/Xm/Xs/#TimeLayout] [Reason/@Layout]");
            sender.sendMessage("§8» §7Ban a user temporary");
            sender.sendMessage("§c/mute [Name] [Reason/@Layout]");
            sender.sendMessage("§8» §7Mute a user permanently");
            sender.sendMessage("§c/tempmute [Name] [Xmo/Xd/Xh/Xm/Xs/#TimeLayout] [Reason/@Layout]");
            sender.sendMessage("§8» §7Mute a user temporary");
            sender.sendMessage("§c/warn [Name] [Reason/@Layout]");
            sender.sendMessage("§8» §7Warn a user permanently");
            sender.sendMessage("§c/tempwarn [Name] [Xmo/Xd/Xh/Xm/Xs/#TimeLayout] [Reason/@Layout]");
            sender.sendMessage("§8» §7Warn a user temporary");
            sender.sendMessage("§c/kick [Name] [Reason/@Layout]");
            sender.sendMessage("§8» §7Kick a user");
            sender.sendMessage("§c/unban [Name/IP]");
            sender.sendMessage("§8» §7Unban a user");
            sender.sendMessage("§c/unmute [Name]");
            sender.sendMessage("§8» §7Unmute a user");
            sender.sendMessage("§c/unwarn [ID] or /unwarn clear [Name]");
            sender.sendMessage("§8» §7Deletes a warn");
            sender.sendMessage("§c/change-reason [ID or ban/mute USER] [New reason]");
            sender.sendMessage("§8» §7Changes the reason of a punishment");
            sender.sendMessage("§c/unpunish [ID]");
            sender.sendMessage("§8» §7Deletes a punishment by ID");
            sender.sendMessage("§c/banlist <Page>");
            sender.sendMessage("§8» §7See all punishments");
            sender.sendMessage("§c/history [Name/IP] <Page>");
            sender.sendMessage("§8» §7See a users history");
            sender.sendMessage("§c/warns [Name] <Page>");
            sender.sendMessage("§8» §7See your or a users wa");
            sender.sendMessage("§c/check [Name]");
            sender.sendMessage("§8» §7Get all information about a user");
            sender.sendMessage("§c/AdvancedBan <reload/help>");
            sender.sendMessage("§8» §7Reloads the plugin or shows help page");
            sender.sendMessage("§8");
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("ab.reload")) {
                sendPermissionMessage(sender);
                return true;
            }
            try {
                sender.getAdvancedBan().loadFiles();
                sender.sendMessage("§a§lAdvancedBan §8§l» §7Reloaded!");
            } catch (IOException e) {
                sender.sendMessage("§cAn error occurred whilst reloading the files. Please check the logs");
                sender.getAdvancedBan().getLogger().warn("§cAn error occurred whilst reloading the files. Please check the logs");
                sender.getAdvancedBan().getLogger().logException(e);
            }
        }
        return true;
    }
}
