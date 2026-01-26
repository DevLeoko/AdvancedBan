package me.leoko.advancedban.bukkit.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Scheduler adapter for Paper, Purpur, and other Paper-based forks.
 * Uses the traditional BukkitScheduler API.
 */
public class PaperSchedulerAdapter implements SchedulerAdapter {
    
    @Override
    public void runAsyncDelayed(Plugin plugin, Runnable task, long delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
    }
    
    @Override
    public void runAsync(Plugin plugin, Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }
    
    @Override
    public void runAsyncRepeating(Plugin plugin, Runnable task, long delay, long period) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period);
    }
    
    @Override
    public void runSync(Plugin plugin, Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }
    
    @Override
    public void runEntityTask(Plugin plugin, Entity entity, Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }
}
