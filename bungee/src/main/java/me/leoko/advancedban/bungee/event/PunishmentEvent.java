package me.leoko.advancedban.bungee.event;

import me.leoko.advancedban.utils.Punishment;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event fired when a punishment is created
 */
public class PunishmentEvent extends Event {
    private final Punishment punishment;
    private final boolean silent;

    public PunishmentEvent(Punishment punishment) {
        this(punishment, false);
    }

    public PunishmentEvent(Punishment punishment, boolean silent) {
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