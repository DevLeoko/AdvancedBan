package me.leoko.advancedban.command;

import me.leoko.advancedban.punishment.PunishmentType;

public class WarningCommand extends PermanentCommand {

    public WarningCommand() {
        super(PunishmentType.WARNING);
    }
}
