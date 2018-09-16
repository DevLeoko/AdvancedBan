package me.leoko.advancedban.command;

import me.leoko.advancedban.punishment.PunishmentType;

public class IPBanCommand extends PermanentCommand {

    IPBanCommand() {
        super(PunishmentType.IP_BAN);
    }
}
