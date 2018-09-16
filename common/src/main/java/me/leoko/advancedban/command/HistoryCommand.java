package me.leoko.advancedban.command;

import me.leoko.advancedban.AdvancedBanCommandSender;
import me.leoko.advancedban.utils.CommandUtils;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public class HistoryCommand extends AbstractCommand {

    HistoryCommand() {
        super("history", "ab.history", "History", "hist");
    }

    @Override
    public boolean onCommand(AdvancedBanCommandSender sender, String[] args) {
        if (args.length > 0) {
            Optional<UUID> uuid = sender.getAdvancedBan().getUuidManager().getUUID(args[0]);
            if (!uuid.isPresent()) {
                sender.sendCustomMessage("General.FailedFetch", true, "NAME", args[0]);
                return true;
            }
            OptionalInt page = OptionalInt.empty();
            if (args.length > 1) {
                page = CommandUtils.parseInt(args[1]);
            }
            performList(sender, page.orElse(1),
                    sender.getAdvancedBan().getPunishmentManager().getPunishments(uuid.get(), null, false),
                    args[0], true);
            return true;
        }
        return false;
    }
}
