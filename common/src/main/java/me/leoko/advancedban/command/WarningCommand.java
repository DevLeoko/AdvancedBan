package me.leoko.advancedban.command;

import me.leoko.advancedban.punishment.PunishmentType;

public class WarningCommand extends PermanentCommand {

    WarningCommand() {
        super(PunishmentType.WARNING);
    }
}
