package me.leoko.advancedban.punishment;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by Leoko @ dev.skamps.eu on 30.05.2016.
 */
public enum PunishmentType {
    BAN("Ban", null, false, "ab.ban.perma", "permaban", "banhammer"),
    TEMP_BAN("Tempban", BAN, true, "ab.ban.temp", "tban"),
    IP_BAN("Ipban", BAN, false, "ab.ipban.perma"),
    TEMP_IP_BAN("Tempipban", BAN, true, "ab.ipban.temp", "tipban"),
    MUTE("Mute", null, false, "ab.mute.perma", "silence"),
    TEMP_MUTE("Tempmute", MUTE, true, "ab.mute.temp", "tmute", "tempsilence", "tsilence"),
    WARNING("Warn", null, false, "ab.warn.perma", "caution"),
    TEMP_WARNING("Tempwarn", WARNING, true, "ab.warn.temp", "twarn", "tempcaution", "tcaution"),
    KICK("Kick", null, false, "ab.kick.use", "boot");

    private final String name;
    private final String command;
    private final String perms;
    private final PunishmentType basic;
    private final boolean temp;
    private String[] aliases;

    PunishmentType(String name, PunishmentType basic, boolean temp, String perms, String... aliases) {
        this.name = name;
        this.command = name.toLowerCase();
        this.basic = basic;
        this.temp = temp;
        this.perms = perms;
        this.aliases = aliases;
    }

    public String getName() {
        return name.toLowerCase();
    }

    public String getCommand() {
        return command;
    }

    public String[] getAliases() {
        return aliases;
    }

    public String getPerms() {
        return perms;
    }

    public String getConfSection() {
        return name;
    }

    public PunishmentType getBasic() {
        return basic == null ? this : basic;
    }

    public boolean isTemp() {
        return temp;
    }

    @JsonValue
    public int getValue() {
        return ordinal();
    }
}