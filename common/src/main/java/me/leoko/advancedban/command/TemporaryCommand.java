package me.leoko.advancedban.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanCommandSender;
import me.leoko.advancedban.AdvancedBanPlayer;
import me.leoko.advancedban.punishment.Punishment;
import me.leoko.advancedban.punishment.PunishmentType;
import me.leoko.advancedban.utils.CommandUtils;

import java.util.Optional;
import java.util.UUID;

public abstract class TemporaryCommand extends PunishmentTypeCommand {

    TemporaryCommand(PunishmentType type) {
        super(type);
    }

    @Override
    public boolean onCommand(AdvancedBanCommandSender sender, String[] args) {
        if (args.length >= 2 && ((args[1].toLowerCase().matches("[1-9][0-9]*([wdhms]|mo)") || args[1].toLowerCase().matches("#.+")))) {

            int reasonBegin = 2;
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

            Object identifier = CommandUtils.getIdentifier(sender, getType(), args[0]);

            final long start = advancedBan.getTimeManager().getTime();
            long end = start;
            if (args[1].matches("#.+")) {
                JsonNode layout = advancedBan.getLayouts().getLayout("Time." + args[1].substring(1));
                if (layout.getNodeType() != JsonNodeType.ARRAY) {
                    sender.sendCustomMessage("General.LayoutNotFound", true, "NAME", args[1].substring(1));
                    return true;
                }
                int i = advancedBan.getPunishmentManager().getCalculationLevel(identifier, args[1].substring(1));
                JsonNode timeNode = layout.get(layout.size() <= i ? layout.size() - 1 : i);

                if (timeNode.getNodeType() != JsonNodeType.STRING) {
                    sender.sendCustomMessage("General.LayoutNotFound", true, "NAME", args[1].substring(1));
                    return true;
                }
                String time = timeNode.textValue();

                end = time.equalsIgnoreCase("perma") ? -1 : end + advancedBan.getTimeManager().toMilliSec(time.toLowerCase());
            } else {
                long toAdd = advancedBan.getTimeManager().toMilliSec(args[1].toLowerCase());
                end += toAdd;
                if (!sender.hasPermission("ab." + getType().getName() + ".dur.max")) {
                    long max = -1;
                    for (int i = 10; i >= 1; i--) {
                        if (sender.hasPermission("ab." + getType().getName() + ".dur." + i)) {
                            Long val = advancedBan.getConfiguration().getTempPerms().get(i);
                            if (val != null) {
                                max = val;
                                break;
                            }
                        }
                    }
                    if (max != -1 && toAdd > max) {
                        sender.sendCustomMessage(getType().getConfSection() + ".MaxDuration", true, "MAX", max / 1000 + "");
                        return true;
                    }
                }
            }

            if (identifier instanceof UUID) {
                Optional<AdvancedBanPlayer> player = advancedBan.getPlayer((UUID) identifier);

                if (player.isPresent() && player.get().hasPermission("ab." + getType().getName() + ".exempt") || advancedBan.getConfiguration().getExemptPlayers().contains(args[0])) {
                    sender.sendCustomMessage(getType().getBasic().getConfSection() + ".Exempt", true, "NAME", args[0]);
                    return true;
                }

                if (!player.isPresent() && getType() == PunishmentType.KICK) {
                    sender.sendCustomMessage("Kick.NotOnline", true, "NAME", args[0]);
                    return true;
                }

                if ((getType().getBasic() == PunishmentType.MUTE && advancedBan.getPunishmentManager().isMuted(identifier))
                        || (getType().getBasic() == PunishmentType.BAN && advancedBan.getPunishmentManager().isBanned(identifier))) {
                    sender.sendCustomMessage(getType().getBasic().getConfSection() + ".AlreadyDone", true, "NAME", args[0]);
                    return true;
                }
            }

            Punishment punishment = new Punishment(advancedBan, identifier, args[0], sender.getName(), null,
                    start, end, getType());
            punishment.setReason(reason);
            punishment.create(silent);
            return true;
        }
        return false;
    }
}
