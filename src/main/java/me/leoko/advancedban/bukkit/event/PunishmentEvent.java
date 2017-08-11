package me.leoko.advancedban.bukkit.event;

import me.leoko.advancedban.utils.Punishment;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a punishment is created
 */
public class PunishmentEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Punishment punishment;

    public PunishmentEvent(Punishment punishment) {
        super(true);
        this.punishment = punishment;
    }

    /**
     * Returns the punishment involved in this event
     *
     * @return Punishment
     */
    public Punishment getPunishment() {
        return punishment;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
