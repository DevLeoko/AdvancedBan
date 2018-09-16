package me.leoko.advancedban.nukkit.event;

import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import me.leoko.advancedban.punishment.Punishment;

/**
 * Event fired when a punishment is created
 */
public class PunishmentEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Punishment punishment;

    public PunishmentEvent(Punishment punishment) {
        this.punishment = punishment;
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
}