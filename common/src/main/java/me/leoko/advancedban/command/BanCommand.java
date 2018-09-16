package me.leoko.advancedban.command;

import me.leoko.advancedban.punishment.PunishmentType;

public class BanCommand extends PermanentCommand {

    BanCommand() {
        super(PunishmentType.BAN);
    }
}
