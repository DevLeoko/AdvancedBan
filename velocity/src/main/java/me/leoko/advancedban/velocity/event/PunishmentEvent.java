package me.leoko.advancedban.velocity.event;

import me.leoko.advancedban.utils.Punishment;

public class PunishmentEvent {
    private final Punishment punishment;

    public PunishmentEvent(Punishment punishment) {
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
}
