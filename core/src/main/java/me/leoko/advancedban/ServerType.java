package me.leoko.advancedban;

public enum ServerType {

  BUNGEECORD("Bungeecord"),
  VELOCITY("Velocity"),
  SPIGOT("Spigot"),
  TEST("Test");

  private String serverType;

  private ServerType(String serverType) {
    this.serverType = serverType;
  }

  public String getString() {
    return this.serverType;
  }

}
