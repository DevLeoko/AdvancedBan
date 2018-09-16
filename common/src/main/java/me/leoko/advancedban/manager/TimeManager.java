package me.leoko.advancedban.manager;

import lombok.RequiredArgsConstructor;
import me.leoko.advancedban.AdvancedBan;

import java.util.concurrent.TimeUnit;

/**
 * Created by Leoko @ dev.skamps.eu on 12.07.2016.
 */
@RequiredArgsConstructor
public class TimeManager {
    private final AdvancedBan advancedBan;

    public long getTime() {
        return System.currentTimeMillis() + TimeUnit.HOURS.toMillis(advancedBan.getConfiguration().getTimeDifferential());
    }

    public long toMilliSec(String s) {
        // This is not my regex :P | From: http://stackoverflow.com/a/8270824
        String[] sl = s.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

        long i = Long.parseLong(sl[0]);

        switch (sl[1]) {
            case "m":
                return TimeUnit.MINUTES.toMillis(i);
            case "h":
                return TimeUnit.HOURS.toMillis(i);
            case "d":
                return TimeUnit.DAYS.toMillis(i);
            case "w":
                return TimeUnit.DAYS.toMillis(i) * 7;
            case "mo":
                return TimeUnit.DAYS.toMillis(i) * 30;
            case "y":
                return TimeUnit.DAYS.toMillis(i) * 365;
            default:
                return TimeUnit.SECONDS.toMillis(i);
        }
    }
}