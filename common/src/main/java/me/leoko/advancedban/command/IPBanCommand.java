package me.leoko.advancedban.command;

import me.leoko.advancedban.punishment.PunishmentType;

public class IPBanCommand extends PermanentCommand {

    public IPBanCommand() {
        super(PunishmentType.IP_BAN);
    }
}
