package me.leoko.advancedban.bukkit.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

/**
 * Scheduler adapter for Folia servers with region-based threading.
 * Uses reflection to access Folia-specific scheduling APIs without compile-time dependencies.
 */
public class FoliaSchedulerAdapter implements SchedulerAdapter {
    
    @Override
    public void runAsyncDelayed(Plugin plugin, Runnable task, long delay) {
        try {
            Object asyncScheduler = Bukkit.getServer().getClass().getMethod("getAsyncScheduler").invoke(Bukkit.getServer());
            long delayMillis = delay * 50;
            asyncScheduler.getClass()
                    .getMethod("runDelayed", Plugin.class, java.util.function.Consumer.class, long.class, TimeUnit.class)
                    .invoke(asyncScheduler, plugin, (java.util.function.Consumer<Object>) t -> task.run(), delayMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to schedule async delayed task on Folia", e);
        }
    }
    
    @Override
    public void runAsync(Plugin plugin, Runnable task) {
        try {
            Object asyncScheduler = Bukkit.getServer().getClass().getMethod("getAsyncScheduler").invoke(Bukkit.getServer());
            asyncScheduler.getClass()
                    .getMethod("runNow", Plugin.class, java.util.function.Consumer.class)
                    .invoke(asyncScheduler, plugin, (java.util.function.Consumer<Object>) t -> task.run());
        } catch (Exception e) {
            throw new RuntimeException("Failed to schedule async task on Folia", e);
        }
    }
    
    @Override
    public void runAsyncRepeating(Plugin plugin, Runnable task, long delay, long period) {
        try {
            Object asyncScheduler = Bukkit.getServer().getClass().getMethod("getAsyncScheduler").invoke(Bukkit.getServer());
            long delayMillis = delay * 50;
            long periodMillis = period * 50;
            asyncScheduler.getClass()
                    .getMethod("runAtFixedRate", Plugin.class, java.util.function.Consumer.class, long.class, long.class, TimeUnit.class)
                    .invoke(asyncScheduler, plugin, (java.util.function.Consumer<Object>) t -> task.run(), delayMillis, periodMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to schedule async repeating task on Folia", e);
        }
    }
    
    @Override
    public void runSync(Plugin plugin, Runnable task) {
        try {
            Object globalRegionScheduler = Bukkit.getServer().getClass().getMethod("getGlobalRegionScheduler").invoke(Bukkit.getServer());
            globalRegionScheduler.getClass()
                    .getMethod("run", Plugin.class, java.util.function.Consumer.class)
                    .invoke(globalRegionScheduler, plugin, (java.util.function.Consumer<Object>) t -> task.run());
        } catch (Exception e) {
            throw new RuntimeException("Failed to schedule sync task on Folia", e);
        }
    }
    
    @Override
    public void runEntityTask(Plugin plugin, Entity entity, Runnable task) {
        try {
            Object scheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
            scheduler.getClass()
                    .getMethod("run", Plugin.class, java.util.function.Consumer.class, Runnable.class)
                    .invoke(scheduler, plugin, (java.util.function.Consumer<Object>) t -> task.run(), null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to schedule entity task on Folia", e);
        }
    }
}
