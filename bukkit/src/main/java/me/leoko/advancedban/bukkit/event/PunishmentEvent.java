package me.leoko.advancedban.bukkit.event;

import lombok.Getter;
import me.leoko.advancedban.core.utils.Punishment;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a punishment is created
 */
@Getter
public class PunishmentEvent extends Event {
	
    private static final HandlerList handlers = new HandlerList();
    
    private final Punishment punishment;

    public PunishmentEvent(Punishment punishment) {
        super(true);
        this.punishment = punishment;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
}