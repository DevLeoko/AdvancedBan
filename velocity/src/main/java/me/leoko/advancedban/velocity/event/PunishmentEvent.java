package me.leoko.advancedban.velocity.event;

import me.leoko.advancedban.utils.Punishment;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PunishmentEvent {

    private final Punishment punishment;

    private final boolean silent;

    public PunishmentEvent(@NonNull Punishment punishment) {
        this(punishment, false);
    }

    public PunishmentEvent(@NonNull Punishment punishment, boolean silent) {
        this.punishment = punishment;
        this.silent = silent;
    }


    public Punishment getPunishment() {
        return punishment;
    }

    public boolean isSilent() {
        return silent;
    }


}
