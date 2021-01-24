package me.leoko.advancedban.sponge7.event;

import me.leoko.advancedban.sponge7.SpongeMain;
import me.leoko.advancedban.utils.Punishment;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.impl.AbstractEvent;

/**
 * Event fired when a punishment is created
 */
public class PunishmentEvent extends AbstractEvent {

    private final Punishment punishment;

    public PunishmentEvent(@NonNull Punishment punishment) {
        this.punishment = punishment;
    }

    public Punishment getPunishment() {
        return this.punishment;
    }

    @Override
    public Cause getCause() {
        return Cause.of(EventContext.builder().add(EventContextKeys.PLUGIN, SpongeMain.get().getPluginContainer()).build(), SpongeMain.get().getPluginContainer());
    }
}
