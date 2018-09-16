package me.leoko.advancedban.nukkit.event;

import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import me.leoko.advancedban.punishment.Punishment;

/**
 * Event fired when a punishment is revoked
 */
public class RevokePunishmentEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Punishment punishment;
    private final boolean massClear;

    public RevokePunishmentEvent(Punishment punishment, boolean massClear) {
        this.punishment = punishment;
        this.massClear = massClear;
    }

    public static HandlerList getHandlers() {
        return handlers;
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