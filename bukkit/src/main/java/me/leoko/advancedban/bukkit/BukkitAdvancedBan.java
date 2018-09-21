package me.leoko.advancedban.bukkit;

import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanPlayer;
import me.leoko.advancedban.bukkit.event.PunishmentEvent;
import me.leoko.advancedban.bukkit.event.RevokePunishmentEvent;
import me.leoko.advancedban.command.AbstractCommand;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.punishment.Punishment;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class BukkitAdvancedBan extends AdvancedBan {
    private final BukkitAdvancedBanPlugin plugin;

    BukkitAdvancedBan(BukkitAdvancedBanPlugin plugin) {
        super(UUIDManager.FetcherMode.MIXED, true);
        this.plugin = plugin;
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public void executeCommand(String command) {
        CommandSender sender = getServer().getConsoleSender();
        getServer().dispatchCommand(sender, command);
    }

    public Server getServer() {
        return plugin.getServer();
    }

    @Override
    public Collection<AdvancedBanPlayer> getOnlinePlayers() {
        for (Player player : getServer().getOnlinePlayers()) {
            if (!getPlayer(player.getUniqueId()).isPresent()) {
                registerPlayer(new BukkitAdvancedBanPlayer(player, this));
            }
        }
        return super.getOnlinePlayers();
    }

    @Override
    public Path getDataFolderPath() {
        return plugin.getDataFolder().toPath();
    }

    @Override
    public void scheduleRepeatingAsyncTask(Runnable runnable, long delay, long period) {
        getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period);
    }

    @Override
    public void scheduleAsyncTask(Runnable runnable, long delay) {
        getServer().getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
    }

    @Override
    public void runAsyncTask(Runnable runnable) {
        getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    @Override
    public void runSyncTask(Runnable runnable) {
        getServer().getScheduler().runTask(plugin, runnable);
    }

    @Override
    public boolean isOnlineMode() {
        return getServer().getOnlineMode();
    }

    @Override
    public void callPunishmentEvent(Punishment punishment) {
        getServer().getPluginManager().callEvent(new PunishmentEvent(punishment));
    }

    @Override
    public void callRevokePunishmentEvent(Punishment punishment, boolean massClear) {
        getServer().getPluginManager().callEvent(new RevokePunishmentEvent(punishment, massClear));
    }

    @Override
    public Optional<UUID> getInternalUUID(String name) {
        OfflinePlayer player = getServer().getOfflinePlayer(name);
        if (player != null) {
            return Optional.ofNullable(player.getUniqueId());
        }
        return Optional.empty();
    }

    @Override
    public boolean isAdvancedBanCommand(String command) {
        command = command.substring(1); // Remove forward slash
        return super.isAdvancedBanCommand(command);
    }

    @Override
    public boolean isMutedCommand(String command) {
        command = command.substring(1); // Remove forward slash
        return super.isMutedCommand(command);
    }

    @Override
    protected void onRegisterCommand(AbstractCommand command) {
        PluginCommand pluginCommand = plugin.getCommand(command.getName());

        pluginCommand.setExecutor(new AdvancedBanCommandExecutor(command, this));
        pluginCommand.setAliases(Arrays.asList(command.getAliases()));
    }

    @Override
    public boolean isUnitTesting() {
        return false;
    }

    @Override
    protected void log(Level level, String msg) {
        plugin.getLogger().log(level, msg);
    }
}
