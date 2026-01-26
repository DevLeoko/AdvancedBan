package me.leoko.advancedban.bukkit.scheduler;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Abstraction layer for scheduling tasks across different server implementations.
 * Provides compatibility with both traditional Paper-based servers and Folia's region-based threading.
 */
public interface SchedulerAdapter {
    
    /**
     * Run a task asynchronously with a delay.
     *
     * @param plugin the plugin
     * @param task the task to run
     * @param delay delay in ticks
     */
    void runAsyncDelayed(Plugin plugin, Runnable task, long delay);
    
    /**
     * Run a task asynchronously immediately.
     *
     * @param plugin the plugin
     * @param task the task to run
     */
    void runAsync(Plugin plugin, Runnable task);
    
    /**
     * Run a repeating task asynchronously.
     *
     * @param plugin the plugin
     * @param task the task to run
     * @param delay initial delay in ticks
     * @param period period in ticks
     */
    void runAsyncRepeating(Plugin plugin, Runnable task, long delay, long period);
    
    /**
     * Run a task on the main/global region thread.
     *
     * @param plugin the plugin
     * @param task the task to run
     */
    void runSync(Plugin plugin, Runnable task);
    
    /**
     * Run a task on the region that owns the specified entity.
     * For non-Folia servers, this runs on the main thread.
     *
     * @param plugin the plugin
     * @param entity the entity whose region should execute the task
     * @param task the task to run
     */
    void runEntityTask(Plugin plugin, Entity entity, Runnable task);
}
