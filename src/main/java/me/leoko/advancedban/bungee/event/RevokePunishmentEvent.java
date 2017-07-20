package me.leoko.advancedban.bungee.event;

import me.leoko.advancedban.utils.Punishment;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event fired when a punishment is revoked
 */
public class RevokePunishmentEvent extends Event {
    private final Punishment punishment;
    private final boolean massClear;

    public RevokePunishmentEvent(Punishment punishment, boolean massClear) {
        this.punishment = punishment;
        this.massClear = massClear;
    }

    /**
     * Returns the punishment involved in this event
     *
     * @return Punishment
     */
    public Punishment getPunishment() {
        return punishment;
    }

    /**
     * If this event is part of a mass clearing of punishments
     * <p>Useful to reduce spam/noise</p>
     *
     * @return True if part of a mass clearing
     */
    public boolean isMassClear() {
        return massClear;
    }
}
