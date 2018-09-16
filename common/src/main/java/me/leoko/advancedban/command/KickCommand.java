package me.leoko.advancedban.command;

import me.leoko.advancedban.punishment.PunishmentType;

public class KickCommand extends PermanentCommand {

    KickCommand() {
        super(PunishmentType.KICK);
    }
}
