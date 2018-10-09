package me.leoko.advancedban.nukkit;

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanPlayer;
import me.leoko.advancedban.command.AbstractCommand;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.nukkit.event.PunishmentEvent;
import me.leoko.advancedban.nukkit.event.RevokePunishmentEvent;
import me.leoko.advancedban.punishment.Punishment;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class NukkitAdvancedBan extends AdvancedBan {
    private final NukkitAdvancedBanPlugin plugin;

    NukkitAdvancedBan(NukkitAdvancedBanPlugin plugin) {
        super(UUIDManager.FetcherMode.INTERNAL, false);
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
        for (Player player : getServer().getOnlinePlayers().values()) {
            if (!getPlayer(player.getUniqueId()).isPresent()) {
                registerPlayer(new NukkitAdvancedBanPlayer(player, this));
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
        getServer().getScheduler().scheduleDelayedRepeatingTask(plugin, runnable, (int) delay, (int) period, true);
    }

    @Override
    public void scheduleAsyncTask(Runnable runnable, long delay) {
        getServer().getScheduler().scheduleDelayedTask(plugin, runnable, (int) delay, true);
    }

    @Override
    public void runAsyncTask(Runnable runnable) {
        getServer().getScheduler().scheduleTask(plugin, runnable, true);
    }

    @Override
    public void runSyncTask(Runnable runnable) {
        getServer().getScheduler().scheduleTask(plugin, runnable, false);
    }

    @Override
    public boolean isOnlineMode() {
        return getServer().getPropertyBoolean("xbox-auth");
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
        IPlayer player = getServer().getOfflinePlayer(name);
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
        getServer().getCommandMap().register("advancedban", new NukkitAdvancedBanCommand(command, this));
    }

    @Override
    public boolean isUnitTesting() {
        return false;
    }

    @Override
    protected void log(Level level, String msg) {
        plugin.getLogger().log(LogLevelConverter.convertLevel(level), msg);
    }
}
