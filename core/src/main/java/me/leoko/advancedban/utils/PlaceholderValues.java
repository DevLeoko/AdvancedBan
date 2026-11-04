package me.leoko.advancedban.utils;

import me.leoko.advancedban.manager.PunishmentManager;

/**
 * The punishment state of a player at a certain point in time, which placeholders can be answered from.
 * Placeholder plugins usually ask on the main thread, so answering must not need the database.
 */
public class PlaceholderValues {
    /**
     * The values of a player without any punishments.
     */
    public static final PlaceholderValues EMPTY = new PlaceholderValues(null, null, 0, 0);

    private final Punishment ban, mute;
    private final int warns, notes;

    private PlaceholderValues(Punishment ban, Punishment mute, int warns, int notes) {
        this.ban = ban;
        this.mute = mute;
        this.warns = warns;
        this.notes = notes;
    }

    /**
     * Get the values of a player from the cache. This does not access the database.
     * It's only meaningful if the player is cached, see {@link PunishmentManager#isCached(String)}.
     *
     * @param uuid the players uuid
     * @return the values
     */
    public static PlaceholderValues fromCache(String uuid) {
        Punishment ban = null, mute = null;
        int warns = 0, notes = 0;
        for (Punishment punishment : PunishmentManager.get().getLoadedPunishments(false)) {
            if (!uuid.equals(punishment.getUuid()) || punishment.isExpired()) {
                continue;
            }
            switch (punishment.getType().getBasic()) {
                case BAN:
                    ban = punishment;
                    break;
                case MUTE:
                    mute = punishment;
                    break;
                case WARNING:
                    warns++;
                    break;
                case NOTE:
                    notes++;
                    break;
                default:
                    break;
            }
        }
        return new PlaceholderValues(ban, mute, warns, notes);
    }

    /**
     * Get the values of a player from the database, so this must not be called from the main thread.
     *
     * @param uuid the players uuid
     * @return the values
     */
    public static PlaceholderValues fromDatabase(String uuid) {
        PunishmentManager manager = PunishmentManager.get();
        return new PlaceholderValues(manager.getBan(uuid), manager.getMute(uuid), manager.getCurrentWarns(uuid), manager.getCurrentNotes(uuid));
    }

    /**
     * Get the value of a placeholder.
     *
     * @param key      the placeholder without its prefix, e.g. <code>ban_reason</code>
     * @param yesValue the value for a positive answer
     * @param noValue  the value for a negative answer
     * @return the value or <code>null</code> if there is no such placeholder
     */
    public String get(String key, String yesValue, String noValue) {
        switch (key.toLowerCase()) {
            case "is_banned":
                return ban != null ? yesValue : noValue;
            case "is_muted":
                return mute != null ? yesValue : noValue;
            case "warns":
                return String.valueOf(warns);
            case "notes":
                return String.valueOf(notes);
            default:
                break;
        }

        if (key.toLowerCase().startsWith("ban_")) {
            return detail(ban, key.substring(4).toLowerCase());
        }
        return key.toLowerCase().startsWith("mute_") ? detail(mute, key.substring(5).toLowerCase()) : null;
    }

    private static String detail(Punishment punishment, String detail) {
        switch (detail) {
            case "reason":
                return punishment == null ? "" : punishment.getReason();
            case "operator":
                return punishment == null ? "" : punishment.getOperator();
            case "id":
                return punishment == null ? "" : String.valueOf(punishment.getId());
            case "date":
                return punishment == null ? "" : punishment.getDate(punishment.getStart());
            case "end":
                if (punishment == null) {
                    return "";
                }
                return punishment.getType().isTemp() ? punishment.getDate(punishment.getEnd()) : punishment.getDuration(false);
            case "remaining":
                return punishment == null ? "" : punishment.getDuration(false);
            default:
                return null;
        }
    }
}
