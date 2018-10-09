package me.leoko.advancedban.command;

import lombok.Getter;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanCommandSender;
import me.leoko.advancedban.AdvancedBanPlayer;
import me.leoko.advancedban.punishment.PunishmentType;
import me.leoko.advancedban.utils.CommandUtils;

import java.util.Optional;
import java.util.UUID;

@Getter
public abstract class PunishmentTypeCommand extends AbstractCommand {
    private final PunishmentType type;

    PunishmentTypeCommand(PunishmentType type) {
        this(type, type.getName());
    }

    PunishmentTypeCommand(PunishmentType type, String name) {
        super(name, type.getPerms(), type.getConfSection(), type.getAliases());
        this.type = type;
    }

    PunishmentTypeCommand(PunishmentType type, String... aliases) {
        super("un" + type.getName(), type.getPerms() + ".undo", "Un" + type.getConfSection(), aliases);
        this.type = type;
    }

    protected boolean canPunish(AdvancedBanCommandSender sender, Object identifier, String arg) {
        AdvancedBan advancedBan = sender.getAdvancedBan();
        if (identifier instanceof UUID) {
            Optional<AdvancedBanPlayer> player = advancedBan.getPlayer((UUID) identifier);
            if (player.isPresent() && player.get().hasPermission("ab." + getType().getName() + ".exempt") || advancedBan.getConfiguration().getExemptPlayers().contains(arg)) {
                sender.sendCustomMessage(getType().getBasic().getConfSection() + ".Exempt", true, "NAME", arg);
                return false;
            }

            if (!player.isPresent() && getType() == PunishmentType.KICK) {
                sender.sendCustomMessage("Kick.NotOnline", true, "NAME", arg);
                return false;
            }
        }

        if ((getType().getBasic() == PunishmentType.MUTE && advancedBan.getPunishmentManager().isMuted(identifier))
                || (getType().getBasic() == PunishmentType.BAN && advancedBan.getPunishmentManager().isBanned(identifier))) {
            sender.sendCustomMessage(getType().getBasic().getConfSection() + ".AlreadyDone", true, "NAME", arg);
            return false;
        }
        return true;
    }

    protected Optional getIdentifier(AdvancedBanCommandSender sender, String arg) {
        if (getType().isIp()) {
            return CommandUtils.getIdentifier(sender.getAdvancedBan(), arg);
        } else {
            return sender.getAdvancedBan().getUuidManager().getUuid(arg);
        }
    }
}
