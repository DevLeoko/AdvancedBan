package me.leoko.advancedban.command;

import me.leoko.advancedban.AdvancedBanCommandSender;
import me.leoko.advancedban.utils.CommandUtils;

import java.util.Optional;
import java.util.OptionalInt;

public class HistoryCommand extends AbstractCommand {

    public HistoryCommand() {
        super("history", "ab.history", "History", "hist");
    }

    @Override
    public boolean onCommand(AdvancedBanCommandSender sender, String[] args) {
        if (args.length > 0 && args.length <= 2) {
            Optional identifier = CommandUtils.getIdentifier(sender.getAdvancedBan(), args[0]);
            if (!identifier.isPresent()) {
                sender.sendCustomMessage("General.FailedFetch", true, "NAME", args[0]);
                return true;
            }
            OptionalInt page = OptionalInt.empty();
            if (args.length > 1) {
                page = CommandUtils.parseInt(args[1]);
            }
            performList(sender, page.orElse(1),
                    sender.getAdvancedBan().getPunishmentManager().getPunishments(identifier.get(), null, false),
                    args[0], true);
            return true;
        }
        return false;
    }
}
