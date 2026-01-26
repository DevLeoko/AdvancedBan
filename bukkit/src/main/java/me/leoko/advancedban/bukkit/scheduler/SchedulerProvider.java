package me.leoko.advancedban.bukkit.scheduler;

import org.bukkit.Bukkit;

/**
 * Factory for obtaining the appropriate scheduler adapter based on server type.
 * Automatically detects Folia vs Paper/Purpur at runtime.
 */
public class SchedulerProvider {
    
    private static SchedulerAdapter instance;
    private static boolean isFolia;
    
    /**
     * Get the scheduler adapter instance for the current server.
     *
     * @return the appropriate scheduler adapter
     */
    public static SchedulerAdapter getScheduler() {
        if (instance == null) {
            instance = createScheduler();
        }
        return instance;
    }
    
    /**
     * Check if the server is running Folia.
     *
     * @return true if Folia is detected
     */
    public static boolean isFolia() {
        if (instance == null) {
            createScheduler();
        }
        return isFolia;
    }
    
    private static SchedulerAdapter createScheduler() {
        try {
            Bukkit.getServer().getClass().getMethod("getGlobalRegionScheduler");
            isFolia = true;
            return new FoliaSchedulerAdapter();
        } catch (NoSuchMethodException e) {
            isFolia = false;
            return new PaperSchedulerAdapter();
        }
    }
}
