package me.leoko.advancedban.command;

import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanCommandSender;
import me.leoko.advancedban.punishment.Punishment;
import me.leoko.advancedban.punishment.PunishmentType;

import java.util.Optional;

public abstract class PermanentCommand extends PunishmentTypeCommand {

    PermanentCommand(PunishmentType type) {
        super(type);
    }

    @Override
    public boolean onCommand(AdvancedBanCommandSender sender, String[] args) {
        if (args.length >= 1) {

            int reasonBegin = 1;
            boolean silent = false;
            if (args.length > reasonBegin && args[reasonBegin].equalsIgnoreCase("-s")) {
                reasonBegin++;
                silent = true;
            }

            AdvancedBan advancedBan = sender.getAdvancedBan();
            String reason = null;

            if (args.length > reasonBegin) {
                reason = buildReason(sender, args, reasonBegin);
                if (reason == null) {
                    return true;
                }
            }

            Optional identifier = getIdentifier(sender, args[0]);

            if (!identifier.isPresent()) {
                sender.sendCustomMessage("General.FailedFetch", true, "NAME", args[0]);
                return true;
            }

            if (!canPunish(sender, identifier.get(), args[0])) {
                return true;
            }

            Punishment punishment = new Punishment(advancedBan, identifier.get(), args[0], sender.getName(), null,
                    advancedBan.getTimeManager().getTime(), -1, getType());
            punishment.setReason(reason);
            punishment.create(silent);
            return true;
        }
        return false;
    }
}
