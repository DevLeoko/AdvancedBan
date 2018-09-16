package me.leoko.advancedban.command;

import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanCommandSender;
import me.leoko.advancedban.AdvancedBanPlayer;
import me.leoko.advancedban.punishment.Punishment;
import me.leoko.advancedban.punishment.PunishmentType;
import me.leoko.advancedban.utils.CommandUtils;

import java.util.Optional;
import java.util.UUID;

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

            Optional identifier = CommandUtils.getIdentifier(sender.getAdvancedBan(), args[0]);
            if (!identifier.isPresent()) {
                return true;
            }

            if (identifier.get() instanceof UUID) {
                Optional<AdvancedBanPlayer> player = advancedBan.getPlayer((UUID) identifier.get());

                if (player.isPresent() && player.get().hasPermission("ab." + getType().getName() + ".exempt") || advancedBan.getConfiguration().getExemptPlayers().contains(args[0])) {
                    sender.sendCustomMessage(getType().getBasic().getConfSection() + ".Exempt", true, "NAME", args[0]);
                    return true;
                }

                if (!player.isPresent() && getType() == PunishmentType.KICK) {
                    sender.sendCustomMessage("Kick.NotOnline", true, "NAME", args[0]);
                    return true;
                }

                if ((getType().getBasic() == PunishmentType.MUTE && advancedBan.getPunishmentManager().isMuted(identifier.get()))
                        || (getType().getBasic() == PunishmentType.BAN && advancedBan.getPunishmentManager().isBanned(identifier.get()))) {
                    sender.sendCustomMessage(getType().getBasic().getConfSection() + ".AlreadyDone", true, "NAME", args[0]);
                    return true;
                }
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
