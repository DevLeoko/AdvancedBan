package me.leoko.advancedban.bukkit.placeholder;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.utils.PlaceholderValues;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides the %advancedban_...% placeholders.
 * This class may only be loaded if PlaceholderAPI is installed.
 * <p>
 * Placeholders get requested on the main thread, possibly many times per second, so they never wait for the database.
 * Online players are answered from the cache of the plugin. The values of offline players get loaded in the
 * background, so the first request for such a player is answered as if there was no punishment.
 */
public class AdvancedBanExpansion extends PlaceholderExpansion {
    private static final long OFFLINE_VALUES_LIFETIME = 30_000;
    private static final int MAX_OFFLINE_ENTRIES = 500;

    private final Plugin plugin;
    private final Set<String> loading = ConcurrentHashMap.newKeySet();
    private final Map<String, OfflineValues> offlineValues = Collections.synchronizedMap(
            new LinkedHashMap<String, OfflineValues>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, OfflineValues> eldest) {
                    return size() > MAX_OFFLINE_ENTRIES;
                }
            });

    private AdvancedBanExpansion(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register the placeholders.
     *
     * @param plugin the plugin
     * @return what has to be done in order to unregister them again
     */
    public static Runnable registerExpansion(Plugin plugin) {
        AdvancedBanExpansion expansion = new AdvancedBanExpansion(plugin);
        expansion.register();
        return expansion::unregister;
    }

    @Override
    public String getIdentifier() {
        return "advancedban";
    }

    @Override
    public String getAuthor() {
        return "Leoko";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null || player.getName() == null) {
            return null;
        }
        return getValues(player.getName().toLowerCase())
                .get(params, PlaceholderAPIPlugin.booleanTrue(), PlaceholderAPIPlugin.booleanFalse());
    }

    private PlaceholderValues getValues(String name) {
        String uuid = UUIDManager.get().getInMemoryUUID(name);
        if (uuid != null && PunishmentManager.get().isCached(uuid)) {
            return PlaceholderValues.fromCache(uuid);
        }

        OfflineValues values = offlineValues.get(name);
        if (values == null || values.isOutdated()) {
            loadInBackground(name);
        }
        return values == null ? PlaceholderValues.EMPTY : values.values;
    }

    private void loadInBackground(String name) {
        if (loading.add(name)) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    String uuid = UUIDManager.get().getUUID(name);
                    offlineValues.put(name, new OfflineValues(uuid == null ? PlaceholderValues.EMPTY : PlaceholderValues.fromDatabase(uuid)));
                } finally {
                    loading.remove(name);
                }
            });
        }
    }

    private static class OfflineValues {
        private final PlaceholderValues values;
        private final long loadedAt = System.currentTimeMillis();

        private OfflineValues(PlaceholderValues values) {
            this.values = values;
        }

        private boolean isOutdated() {
            return System.currentTimeMillis() - loadedAt > OFFLINE_VALUES_LIFETIME;
        }
    }
}
