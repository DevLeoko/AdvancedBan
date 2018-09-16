package me.leoko.advancedban.command;

import lombok.Getter;
import me.leoko.advancedban.punishment.PunishmentType;

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
}
