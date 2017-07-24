package me.leoko.advancedban.bukkit.event;

import me.leoko.advancedban.utils.Punishment;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a punishment is revoked
 */
public class RevokePunishmentEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Punishment punishment;
    private final boolean massClear;

    public RevokePunishmentEvent(Punishment punishment, boolean massClear) {
        super(true);
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

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
