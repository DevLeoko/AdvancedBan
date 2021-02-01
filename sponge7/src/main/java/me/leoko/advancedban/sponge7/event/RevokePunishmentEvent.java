package me.leoko.advancedban.sponge7.event;

import me.leoko.advancedban.sponge7.SpongeMain;
import me.leoko.advancedban.utils.Punishment;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.impl.AbstractEvent;

public class RevokePunishmentEvent extends AbstractEvent {
    private final Punishment punishment;
    private final boolean massClear;

    public RevokePunishmentEvent(@NonNull Punishment punishment, boolean massClear) {
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

    @Override
    public Cause getCause() {
        return Cause.of(EventContext.builder().add(EventContextKeys.PLUGIN, SpongeMain.get().getPluginContainer()).build(), SpongeMain.get().getPluginContainer());
    }

}
