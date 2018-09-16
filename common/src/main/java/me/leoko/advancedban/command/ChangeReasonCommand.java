package me.leoko.advancedban.command;

import me.leoko.advancedban.AdvancedBanCommandSender;
import me.leoko.advancedban.punishment.Punishment;

import java.util.Optional;
import java.util.UUID;

public class ChangeReasonCommand extends AbstractCommand {

    ChangeReasonCommand() {
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
            Optional<UUID> uuid = Optional.empty();
            if (!args[1].matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
                uuid = sender.getAdvancedBan().getUuidManager().getUUID(args[1]);
            }
            if (!uuid.isPresent()) {
                sender.sendCustomMessage("General.FetchFailed", true, "NAME", args[1]);
                return true;
            }

            if (args[0].equalsIgnoreCase("ban")) {
                punishment = sender.getAdvancedBan().getPunishmentManager().getBan(uuid.get());
            } else {
                punishment = sender.getAdvancedBan().getPunishmentManager().getMute(uuid.get());
            }
        } else {
            return false;
        }

        String reason = buildReason(sender, args, reasonStart);
        if (reason != null) {
            if (punishment.isPresent()) {
                punishment.get().updateReason(reason);
                sender.sendCustomMessage("ChangeReason.Done", true, "ID", punishment.get().getId());
            } else {
                sender.sendCustomMessage("ChangeReason.NotFound", true);
            }
        }
        return true;
    }
}
