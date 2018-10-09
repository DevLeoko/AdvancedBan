package me.leoko.advancedban.punishment;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by Leoko @ dev.skamps.eu on 30.05.2016.
 */
public enum PunishmentType {
    BAN("Ban", null, "ab.ban.perma", false, false, "permaban", "banhammer"),
    TEMP_BAN("Tempban", BAN, "ab.ban.temp", true, false, "tban"),
    IP_BAN("Ipban", BAN, "ab.ipban.perma", false, true),
    TEMP_IP_BAN("Tempipban", BAN, "ab.ipban.temp", true, true, "tipban"),
    MUTE("Mute", null, "ab.mute.perma", false, false, "silence"),
    TEMP_MUTE("Tempmute", MUTE, "ab.mute.temp", true, false, "tmute", "tempsilence", "tsilence"),
    WARNING("Warn", null, "ab.warn.perma", false, false, "caution"),
    TEMP_WARNING("Tempwarn", WARNING, "ab.warn.temp", true, false, "twarn", "tempcaution", "tcaution"),
    KICK("Kick", null, "ab.kick.use", false, false, "boot");

    private final String name;
    private final String command;
    private final String perms;
    private final PunishmentType basic;
    private final boolean temp;
    private final boolean ip;
    private String[] aliases;

    PunishmentType(String name, PunishmentType basic, String perms, boolean temp, boolean ip, String... aliases) {
        this.name = name;
        this.command = name.toLowerCase();
        this.basic = basic;
        this.perms = perms;
        this.temp = temp;
        this.ip = ip;
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

    public boolean isIp() {
        return ip;
    }

    @JsonValue
    public int getValue() {
        return ordinal();
    }
}