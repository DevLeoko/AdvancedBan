package me.leoko.advancedban.command;

import me.leoko.advancedban.punishment.PunishmentType;

public class TempBanCommand extends TemporaryCommand {

    public TempBanCommand() {
        super(PunishmentType.TEMP_BAN);
    }
}
