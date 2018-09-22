package me.leoko.advancedban.command;

import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.punishment.Punishment;
import me.leoko.advancedban.punishment.PunishmentType;

import java.util.Optional;

public class UnbanCommand extends UnpunishmentTypeCommand {

    public UnbanCommand() {
        super(PunishmentType.BAN);
    }

    @Override
    public Optional<Punishment> getPunishment(AdvancedBan advancedBan, Object identifier) {
        return advancedBan.getPunishmentManager().getInterimBan(identifier);
    }
}
