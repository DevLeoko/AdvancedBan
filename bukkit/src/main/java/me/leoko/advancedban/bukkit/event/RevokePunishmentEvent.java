package me.leoko.advancedban.bukkit.event;

import lombok.Getter;
import me.leoko.advancedban.core.utils.Punishment;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a punishment is revoked
 */
@Getter
public class RevokePunishmentEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Punishment punishment;
    private final boolean massClear;

    public RevokePunishmentEvent(Punishment punishment, boolean massClear) {
        super(true);
        this.punishment = punishment;
        this.massClear = massClear;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}