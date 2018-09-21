package me.leoko.advancedban.command;

import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanCommandSender;
import me.leoko.advancedban.punishment.Punishment;
import me.leoko.advancedban.punishment.PunishmentType;
import me.leoko.advancedban.utils.CommandUtils;

import java.util.Optional;

public abstract class UnpunishmentTypeCommand extends PunishmentTypeCommand {

    UnpunishmentTypeCommand(PunishmentType type, String... aliases) {
        super(type, aliases);
    }

    @Override
    public boolean onCommand(AdvancedBanCommandSender sender, String[] args) {
        if (args.length == 1) {
            Optional identifier = CommandUtils.getIdentifier(sender.getAdvancedBan(), args[0]);

            if (!identifier.isPresent()) {
                sender.sendCustomMessage("General.FailedFetch", true, "NAME", args[0]);
                return true;
            }

            Optional<Punishment> punishment = getPunishment(sender.getAdvancedBan(), identifier.get());
            if (punishment.isPresent()) {
                punishment.get().delete(sender.getName(), true, true);
                sender.sendCustomMessage(getConfigSection() + ".Done", true, "NAME", args[0]);
            } else {
                sender.sendCustomMessage(getConfigSection() + ".NotPunished", true, "NAME", args[0]);
            }
            return true;
        }
        return false;
    }

    public Optional<Punishment> getPunishment(AdvancedBan advancedBan, Object identifier) {
        throw new UnsupportedOperationException();
    }
}
