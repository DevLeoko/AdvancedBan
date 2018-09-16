package me.leoko.advancedban.nukkit;

import cn.nukkit.utils.LogLevel;
import lombok.experimental.UtilityClass;

import java.util.logging.Level;

@UtilityClass
public class LogLevelConverter {

    public static LogLevel convertLevel(Level level) {
        LogLevel out = LogLevel.INFO;

        if (level.intValue() < Level.INFO.intValue()) {
            out = LogLevel.DEBUG;
        } else if (level == Level.WARNING) {
            out = LogLevel.ALERT;
        } else if (level == Level.SEVERE) {
            out = LogLevel.CRITICAL;
        }
        return out;
    }
}
