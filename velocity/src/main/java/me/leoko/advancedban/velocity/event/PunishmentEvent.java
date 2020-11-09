package me.leoko.advancedban.velocity.event;

import me.leoko.advancedban.utils.Punishment;

/**
 * Event fired when a punishment is created
 */
public class PunishmentEvent {

  private final Punishment punishment;

  public PunishmentEvent(Punishment punishment) {
    this.punishment = punishment;
  }


  public Punishment getPunishment() {
    return this.punishment;
  }
}