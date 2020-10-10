package me.leoko.advancedban.utils.punishment;

import me.leoko.advancedban.manager.TimeManager;
import me.leoko.advancedban.utils.abstraction.Target;

public class Duration {
    public static final Duration INDEFINITE = new Duration(-1);

    private long endTime;
    private String calculation;

    public Duration(int duration, TimeUnit unit){
        endTime = TimeManager.getTime() + TimeManager.toMilliSec(duration+unit.symbol);
    }

    public Duration(long endTime) {
        this.endTime = endTime;
    }

    public static Duration ofCalculation(String calculation, Target target){
        //TODO
        return null;
    }

    public enum TimeUnit {
        SECONDS("s"),
        MINUTES("m"),
        HOURS("h"),
        DAYS("d"),
        WEEKS("w"),
        MONTHS("mo");

        private String symbol;

        TimeUnit(String symbol) {
            this.symbol = symbol;
        }
    }
}
