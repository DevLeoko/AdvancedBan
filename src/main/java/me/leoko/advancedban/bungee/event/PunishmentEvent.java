package me.leoko.advancedban.bungee.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.leoko.advancedban.utils.Punishment;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event fired when a punishment is created
 */
@AllArgsConstructor
public class PunishmentEvent extends Event {
    private @Getter final Punishment punishment;
}