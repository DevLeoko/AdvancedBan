package me.leoko.advancedban.bungee;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanPlayer;
import me.leoko.advancedban.bungee.event.PunishmentEvent;
import me.leoko.advancedban.bungee.event.RevokePunishmentEvent;
import me.leoko.advancedban.command.AbstractCommand;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.punishment.Punishment;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetAddress;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


public class BungeeAdvancedBan extends AdvancedBan {
    private final BungeeAdvancedBanPlugin plugin;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<RedisBungeeAPI> redisBungee = Optional.empty();

    BungeeAdvancedBan(BungeeAdvancedBanPlugin plugin) {
        super(UUIDManager.FetcherMode.INTERNAL, true);
        this.plugin = plugin;
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public void executeCommand(String command) {
        CommandSender sender = getProxy().getConsole();
        getProxy().getPluginManager().dispatchCommand(sender, command);
    }

    @Override
    public Collection<AdvancedBanPlayer> getOnlinePlayers() {
        for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
            if (!getPlayer(player.getUniqueId()).isPresent()) {
                registerPlayer(new BungeeAdvancedBanPlayer(player, this, getProxy()));
            }
        }
        return super.getOnlinePlayers();
    }

    @Override
    public Path getDataFolderPath() {
        return plugin.getDataFolder().toPath();
    }

    @Override
    public boolean isOnline(String name) {
        if (redisBungee.isPresent()) {
            for (String redisName : RedisBungee.getApi().getHumanPlayersOnline()) {
                if (redisName.equalsIgnoreCase(name)) {
                    return RedisBungee.getApi().getPlayerIp(RedisBungee.getApi().getUuidFromName(redisName)) != null;
                }
            }
            return false;
        } else {
            return super.isOnline(name);
        }
    }

    @Override
    public boolean isOnline(UUID uuid) {
        if (redisBungee.isPresent()) {
            for (UUID redisUuid : redisBungee.get().getPlayersOnline()) {
                if (redisUuid.equals(uuid)) {
                    return true;
                }
            }
            return false;
        } else {
            return super.isOnline(uuid);
        }
    }

    @Override
    public void scheduleRepeatingAsyncTask(Runnable runnable, long delay, long period) {
        plugin.getProxy().getScheduler().schedule(plugin, runnable, delay * 50, period * 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void scheduleAsyncTask(Runnable runnable, long delay) {
        plugin.getProxy().getScheduler().schedule(plugin, runnable, delay * 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void runAsyncTask(Runnable runnable) {
        plugin.getProxy().getScheduler().runAsync(plugin, runnable);
    }

    @Override
    public void runSyncTask(Runnable runnable) {
        plugin.getProxy().getScheduler().schedule(plugin, runnable, 0, TimeUnit.NANOSECONDS);
    }

    @Override
    public boolean isOnlineMode() {
        return plugin.getProxy().getConfig().isOnlineMode();
    }

    @Override
    public void callPunishmentEvent(Punishment punishment) {
        getProxy().getPluginManager().callEvent(new PunishmentEvent(punishment));
    }

    @Override
    public void callRevokePunishmentEvent(Punishment punishment, boolean massClear) {
        getProxy().getPluginManager().callEvent(new RevokePunishmentEvent(punishment, massClear));
    }

    @Override
    public Optional<UUID> getInternalUUID(String name) {
        return getPlayer(name).map(AdvancedBanPlayer::getUniqueId);
    }

    @Override
    public void notify(String permission, Collection<String> notifications) {
        if (redisBungee.isPresent()) {
            notifications.forEach((str) -> redisBungee.get().sendChannelMessage("AdvancedBan", "notification " + permission + " " + str));
        } else {
            for (AdvancedBanPlayer player : getOnlinePlayers()) {
                if (player.hasPermission(permission)) {
                    notifications.forEach(player::sendMessage);
                }
            }
        }
    }

    @Override
    public Optional<String> onPreLogin(String name, UUID uuid, InetAddress address) {
        redisBungee.ifPresent(redisBungeeAPI -> {
            ByteBuf buf = Unpooled.buffer(32);
            RedisMessageUtils.writeConnectionMessage(buf, name, uuid, address);
            String payload = Base64.getEncoder().encodeToString(buf.array());
            redisBungeeAPI.sendChannelMessage("AdvancedBan", payload);
        });
        return super.onPreLogin(name, uuid, address);
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
        getProxy().getPluginManager().registerCommand(plugin, new BungeeAdvancedBanCommand(command, this));
    }

    @Override
    public boolean isUnitTesting() {
        return false;
    }

    @Override
    protected void log(Level level, String msg) {
        plugin.getLogger().log(level, msg);
    }

    public ProxyServer getProxy() {
        return plugin.getProxy();
    }

    public Optional<RedisBungeeAPI> getRedisBungee() {
        return redisBungee;
    }

    void setRedisBungee(RedisBungeeAPI redisBungee) {
        this.redisBungee = Optional.ofNullable(redisBungee);
    }
}
