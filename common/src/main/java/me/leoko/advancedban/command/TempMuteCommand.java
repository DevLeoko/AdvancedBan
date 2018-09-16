package me.leoko.advancedban.command;

import me.leoko.advancedban.punishment.PunishmentType;

public class TempMuteCommand extends TemporaryCommand {

    TempMuteCommand() {
        super(PunishmentType.TEMP_MUTE);
    }
}
