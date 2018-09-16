package me.leoko.advancedban.command;

import me.leoko.advancedban.punishment.PunishmentType;

public class TempWarningCommand extends TemporaryCommand {

    TempWarningCommand() {
        super(PunishmentType.TEMP_WARNING);
    }
}
