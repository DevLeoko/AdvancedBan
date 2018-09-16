package me.leoko.advancedban.command;

import me.leoko.advancedban.punishment.PunishmentType;

public class TempIPBanCommand extends TemporaryCommand {

    TempIPBanCommand() {
        super(PunishmentType.TEMP_IP_BAN);
    }
}
