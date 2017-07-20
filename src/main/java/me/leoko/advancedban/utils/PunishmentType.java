package me.leoko.advancedban.utils;

/**
 * Created by Leoko @ dev.skamps.eu on 30.05.2016.
 */
public enum PunishmentType {
    BAN("Ban", null, false, "ab.ban.perma"),
    TEMP_BAN("Tempban", PunishmentType.BAN, true, "ab.ban.temp"),
    IP_BAN("Ipban", PunishmentType.BAN, false, "ab.ban.ip"),
    MUTE("Mute", null, false, "ab.mute.perma"),
    TEMP_MUTE("Tempmute", PunishmentType.MUTE, true, "ab.mute.temp"),
    WARNING("Warn", null, false, "ab.warn.perma"),
    TEMP_WARNING("Tempwarn", PunishmentType.WARNING, true, "ab.warn.temp"),
    KICK("Kick", null, false, "ab.kick.use");

    private final String name;
    private final String perms;
    private final PunishmentType basic;
    private final boolean temp;

    PunishmentType(String name, PunishmentType basic, boolean temp, String perms) {
        this.name = name;
        this.basic = basic;
        this.temp = temp;
        this.perms = perms;
    }

    public static PunishmentType fromCommandName(String cmd) {
        switch (cmd) {
            case "ban":
                return PunishmentType.BAN;
            case "tempban":
                return PunishmentType.TEMP_BAN;
            case "banip":
            case "ipban":
                return PunishmentType.IP_BAN;
            case "mute":
                return PunishmentType.MUTE;
            case "tempmute":
                return PunishmentType.TEMP_MUTE;
            case "warn":
                return PunishmentType.WARNING;
            case "tempwarn":
                return PunishmentType.TEMP_WARNING;
            case "kick":
                return PunishmentType.KICK;
            default:
                return null;
        }
    }

    public String getName() {
        return name.toLowerCase();
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
}
