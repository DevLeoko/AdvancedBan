package me.leoko.advancedban.velocity.event;

import me.leoko.advancedban.utils.Punishment;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Event fired when a punishment is created
 */
public class PunishmentEvent {

  private final Punishment punishment;

  public PunishmentEvent(@NonNull Punishment punishment) {
    this.punishment = punishment;
  }


  public Punishment getPunishment() {
    return this.punishment;
  }
}