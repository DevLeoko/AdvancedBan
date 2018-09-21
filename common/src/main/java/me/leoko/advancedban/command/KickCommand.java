package me.leoko.advancedban.command;

import me.leoko.advancedban.punishment.PunishmentType;

public class KickCommand extends PermanentCommand {

    public KickCommand() {
        super(PunishmentType.KICK);
    }
}
