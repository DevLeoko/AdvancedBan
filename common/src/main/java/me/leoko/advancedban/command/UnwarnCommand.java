package me.leoko.advancedban.command;

import me.leoko.advancedban.AdvancedBanCommandSender;
import me.leoko.advancedban.punishment.Punishment;
import me.leoko.advancedban.punishment.PunishmentType;
import me.leoko.advancedban.utils.CommandUtils;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public class UnwarnCommand extends UnpunishmentTypeCommand {

    public UnwarnCommand() {
        super(PunishmentType.WARNING);
    }

    @Override
    public boolean onCommand(AdvancedBanCommandSender sender, String[] args) {
        if (args.length > 0) {
            if (args.length == 1 && args[0].matches("[0-9]+")) {
                OptionalInt id = CommandUtils.parseInt(args[0]);

                if (!id.isPresent()) {
                    return false;
                }

                Optional<Punishment> punishment = sender.getAdvancedBan().getPunishmentManager().getWarn(id.getAsInt());
                if (punishment.isPresent()) {
                    sender.getAdvancedBan().getPunishmentManager().deletePunishment(punishment.get());
                    sender.sendCustomMessage(getConfigSection() + ".Done", true, "ID", args[0]);
                } else {
                    sender.sendCustomMessage(getConfigSection() + ".NotFound", true, "ID", args[0]);
                }
                return true;
            } else if (args.length == 2 && args[0].equalsIgnoreCase("clear")) {
                Optional<UUID> uuid = sender.getAdvancedBan().getUuidManager().getUUID(args[1]);

                if (!uuid.isPresent()) {
                    sender.sendCustomMessage("General.FailedFetch", true, "NAME", args[1]);
                    return true;
                }

                List<Punishment> punishments = sender.getAdvancedBan().getPunishmentManager().getWarns(uuid.get());
                if (!punishments.isEmpty()) {
                    for (Punishment punishment : punishments) {
                        sender.getAdvancedBan().getPunishmentManager().deletePunishment(punishment, true);
                    }
                    sender.sendCustomMessage(getConfigSection() + ".Clear.Done", true, "COUNT", punishments.size());
                } else {
                    sender.sendCustomMessage(getConfigSection() + ".Clear.Empty", true, "NAME", args[1]);
                }
                return true;
            }
        }
        return false;
    }
}
