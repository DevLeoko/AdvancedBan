package me.leoko.advancedban.command;

import me.leoko.advancedban.punishment.PunishmentType;

public class TempBanCommand extends TemporaryCommand {

    TempBanCommand() {
        super(PunishmentType.TEMP_BAN);
    }
}
