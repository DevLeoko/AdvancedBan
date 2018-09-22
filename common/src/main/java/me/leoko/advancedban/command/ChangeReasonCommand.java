package me.leoko.advancedban.command;

import me.leoko.advancedban.AdvancedBanCommandSender;
import me.leoko.advancedban.punishment.Punishment;
import me.leoko.advancedban.utils.CommandUtils;

import java.util.Optional;

public class ChangeReasonCommand extends AbstractCommand {

    public ChangeReasonCommand() {
        super("change-reason", "ab.changereason", "ChangeReason", "changereason", "setreason", "modifyreason");
    }

    @Override
    public boolean onCommand(AdvancedBanCommandSender sender, String[] args) {
        Optional<Punishment> punishment;

        int reasonStart;
        if (args.length > 1 && args[0].matches("[0-9]*")) {
            punishment = sender.getAdvancedBan().getPunishmentManager().getPunishment(Integer.parseInt(args[0]));
            reasonStart = 1;
        } else if (args.length > 2 && args[0].toLowerCase().matches("mute|ban")) {
            reasonStart = 2;
            Optional identifier = CommandUtils.getIdentifier(sender.getAdvancedBan(), args[1]);
            if (!identifier.isPresent()) {
                sender.sendCustomMessage("General.FetchFailed", true, "NAME", args[1]);
                return true;
            }

            if (args[0].equalsIgnoreCase("ban")) {
                punishment = sender.getAdvancedBan().getPunishmentManager().getInterimBan(identifier.get());
            } else {
                punishment = sender.getAdvancedBan().getPunishmentManager().getMute(identifier.get());
            }
        } else {
            return false;
        }

        String reason = buildReason(sender, args, reasonStart);
        if (reason != null) {
            if (punishment.isPresent()) {
                punishment.get().setReason(reason);
                sender.getAdvancedBan().getPunishmentManager().updatePunishment(punishment.get());
                sender.sendCustomMessage("ChangeReason.Done", true, "ID", punishment.get().getId().orElse(-1));
            } else {
                sender.sendCustomMessage("ChangeReason.NotFound", true);
            }
        }
        return true;
    }
}
