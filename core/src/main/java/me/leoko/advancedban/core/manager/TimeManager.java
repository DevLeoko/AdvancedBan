package me.leoko.advancedban.core.manager;


import me.leoko.advancedban.core.Universal;

import java.util.Date;

/**
 * The Time Manager is used to have a centralized time for advanced ban which can be different from the system's time.
 */
public class TimeManager {
    /**
     * Get the current timestamp in milliseconds.
     *
     * @return the timestamp
     */
    public static long getTime() {
        return new Date().getTime() + Universal.get().getMethods().getInteger(Universal.get().getMethods().getConfig(), "TimeDiff", 0) * 60 * 60 * 1000;
    }

    /**
     * Convert a Time String to the amount of milliseconds.
     * These Strings are used for the temporary advancedban punish commands.
     *
     * @param s the time string
     * @return the amount of milliseconds equivalent to the given string
     */
    public static long toMilliSec(String s) {
        // This is not my regex :P | From: http://stackoverflow.com/a/8270824
        String[] sl = s.toLowerCase().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

        long i = Long.parseLong(sl[0]);
        switch (sl[1]) {
            case "s":
                return i * 1000;
            case "m":
                return i * 1000 * 60;
            case "h":
                return i * 1000 * 60 * 60;
            case "d":
                return i * 1000 * 60 * 60 * 24;
            case "w":
                return i * 1000 * 60 * 60 * 24 * 7;
            case "mo":
                return i * 1000 * 60 * 60 * 24 * 30;
            default:
                return -1;
        }
    }
}