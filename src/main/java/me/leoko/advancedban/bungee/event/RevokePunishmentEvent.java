package me.leoko.advancedban.bungee.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.leoko.advancedban.utils.Punishment;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event fired when a punishment is revoked
 */

@AllArgsConstructor
@Getter
public class RevokePunishmentEvent extends Event {
    private final Punishment punishment;
    private final boolean massClear;
}