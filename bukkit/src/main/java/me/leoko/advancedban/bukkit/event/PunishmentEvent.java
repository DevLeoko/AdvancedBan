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
    private final boolean silent;

    public PunishmentEvent(Punishment punishment) {
        this(punishment, false);
    }

    public PunishmentEvent(Punishment punishment, boolean silent) {
        super(false);
        this.punishment = punishment;
        this.silent = silent;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Punishment getPunishment() {
        return this.punishment;
    }

    public boolean isSilent() {
        return silent;
    }
}
