package me.leoko.advancedban.command;

import me.leoko.advancedban.punishment.PunishmentType;

public class MuteCommand extends PermanentCommand {

    MuteCommand() {
        super(PunishmentType.MUTE);
    }
}
