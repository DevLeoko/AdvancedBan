package me.leoko.advancedban.command;

import me.leoko.advancedban.AdvancedBanCommandSender;
import me.leoko.advancedban.utils.SQLQuery;

public class BanlistCommand extends AbstractCommand {

    public BanlistCommand() {
        super("banlist", "ab.banlist", "Banlist");
    }

    @Override
    public boolean onCommand(AdvancedBanCommandSender sender, String[] args) {
        if (args.length == 0 || args.length == 1 && args[0].toLowerCase().matches("[1-9][0-9]*")) {
            performList(
                    sender,
                    args.length == 0 ? 1 : Integer.valueOf(args[0].toLowerCase()),
                    sender.getAdvancedBan().getPunishmentManager().getPunishments(SQLQuery.SELECT_ALL_PUNISHMENTS_LIMIT, 150),
                    null,
                    false);
            return true;
        }
        return false;
    }
}
