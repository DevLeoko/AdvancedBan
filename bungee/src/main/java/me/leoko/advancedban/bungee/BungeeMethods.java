package me.leoko.advancedban.bungee;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.bungee.event.PunishmentEvent;
import me.leoko.advancedban.bungee.event.RevokePunishmentEvent;
import me.leoko.advancedban.bungee.listener.CommandReceiverBungee;
import me.leoko.advancedban.bungee.utils.CloudNetCloudPermsOfflineUser;
import me.leoko.advancedban.bungee.utils.LuckPermsOfflineUser;
import me.leoko.advancedban.bungee.utils.RedisBungeeHook;
import me.leoko.advancedban.manager.DatabaseManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.utils.Permissionable;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.tabcompletion.TabCompleter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.SimplePie;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created by Leoko @ dev.skamps.eu on 23.07.2016.
 */
public class BungeeMethods implements MethodInterface {

    private static final int BSTATS_PLUGIN_ID = 1226;

    private final File configFile = new File(getDataFolder(), "config.yml");
    private final File messageFile = new File(getDataFolder(), "Messages.yml");
    private final File layoutFile = new File(getDataFolder(), "Layouts.yml");
    private final File mysqlFile = new File(getDataFolder(), "MySQL.yml");
    private Configuration config;
    private Configuration messages;
    private Configuration layouts;
    private Configuration mysql;

    private final Function<String, Permissionable> permissionableGenerator;

    public BungeeMethods() {
        if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {
            permissionableGenerator = LuckPermsOfflineUser::new;

            log("[AdvancedBan] Offline permission support through LuckPerms active");
        } else if (ProxyServer.getInstance().getPluginManager().getPlugin("CloudNet-CloudPerms") != null) {
            permissionableGenerator = CloudNetCloudPermsOfflineUser::new;

            log("[AdvancedBan] Offline permission support through CloudNet-CloudPerms active");
        } else {
            permissionableGenerator = null;

            log("[AdvancedBan] No offline permission support through LuckPerms or CloudNet-CloudPerms");
        }
    }

    @Override
    public void loadFiles() {
        try {
            if (!getDataFolder().exists()) {
                //noinspection ResultOfMethodCallIgnored
                getDataFolder().mkdirs();
            }
            if (!configFile.exists()) {
                Files.copy(getPlugin().getResourceAsStream("config.yml"), configFile.toPath());
            }
            if (!messageFile.exists()) {
                Files.copy(getPlugin().getResourceAsStream("Messages.yml"), messageFile.toPath());
            }
            if (!layoutFile.exists()) {
                Files.copy(getPlugin().getResourceAsStream("Layouts.yml"), layoutFile.toPath());
            }

            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            messages = ConfigurationProvider.getProvider(YamlConfiguration.class).load(messageFile);
            layouts = ConfigurationProvider.getProvider(YamlConfiguration.class).load(layoutFile);

            if (mysqlFile.exists()) {
                mysql = ConfigurationProvider.getProvider(YamlConfiguration.class).load(mysqlFile);
            } else {
                mysql = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getVersion() {
        return getPlugin().getDescription().getVersion();
    }

    @Override
    public String[] getKeys(Object file, String path) {
        return ((Configuration) file).getSection(path).getKeys().toArray(new String[0]);
    }

    @Override
    public Configuration getConfig() {
        return config;
    }

    @Override
    public Configuration getMessages() {
        return messages;
    }

    @Override
    public Configuration getLayouts() {
        return layouts;
    }

    @Override
    public void setupMetrics() {
        Metrics metrics = new Metrics(getPlugin(), BSTATS_PLUGIN_ID);
        metrics.addCustomChart(new SimplePie("MySQL", () -> DatabaseManager.get().isUseMySQL() ? "yes" : "no"));
    }

    @Override
    public boolean isBungee() {
        return true;
    }

    @Override
    public String clearFormatting(String text) {
        return ChatColor.stripColor(text);
    }

    @Override
    public Plugin getPlugin() {
        return BungeeMain.get();
    }

    @Override
    public File getDataFolder() {
        return getPlugin().getDataFolder();
    }

    @Override
    public void setCommandExecutor(String cmd, String permission, TabCompleter tabCompleter) {
        ProxyServer.getInstance().getPluginManager().registerCommand(getPlugin(), new CommandReceiverBungee(cmd, permission));
    }

    @Override
    public void sendMessage(Object player, String msg) {
        ((CommandSender) player).sendMessage(TextComponent.fromLegacyText(msg));
    }

    @Override
    public boolean hasPerms(Object player, String perms) {
        return player != null && ((CommandSender) player).hasPermission(perms);
    }

    @Override
    public Permissionable getOfflinePermissionPlayer(String name) {
        if (permissionableGenerator != null) {
            return permissionableGenerator.apply(name);
        }

        return permission -> false;
    }

    @Override
    public boolean isOnline(String name) {
        if (getPlayer(name) != null) {
            return true;
        }
        if (Universal.isRedis()) {
            for (String str : RedisBungeeHook.getApi().getHumanPlayersOnline()) {
                if (str.equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ProxiedPlayer getPlayer(String name) {
        return ProxyServer.getInstance().getPlayer(name);
    }


    @Override
    public void kickPlayer(String player, String reason) {
        ProxiedPlayer target = getPlayer(player);
        if (target != null) {
            target.disconnect(TextComponent.fromLegacyText(reason));
        } else if (Universal.isRedis()) {
            // The player might be connected to another proxy
            RedisBungeeHook.getApi().sendChannelMessage("advancedban:main", "kick " + player + " " + reason);
        }
    }

    @Override
    public ProxiedPlayer[] getOnlinePlayers() {
        return ProxyServer.getInstance().getPlayers().toArray(new ProxiedPlayer[]{});
    }

    @Override
    public void scheduleAsyncRep(Runnable rn, long l1, long l2) {
        ProxyServer.getInstance().getScheduler().schedule(getPlugin(), rn, l1 * 50, l2 * 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void scheduleAsync(Runnable rn, long l1) {
        ProxyServer.getInstance().getScheduler().schedule(getPlugin(), rn, l1 * 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void runAsync(Runnable rn) {
        ProxyServer.getInstance().getScheduler().runAsync(getPlugin(), rn);
    }

    @Override
    public void runSync(Runnable rn) {
        rn.run(); //TODO WARNING not Sync to Main-Thread
    }

    @Override
    public void executeCommand(String cmd) {
        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), cmd);
    }

    @Override
    public String getName(Object player) {
        return ((CommandSender) player).getName();
    }

    @Override
    public String getName(String uuid) {
        UUID parsedUuid = UUIDManager.get().fromString(uuid);
        ProxiedPlayer player = parsedUuid == null ? null : ProxyServer.getInstance().getPlayer(parsedUuid);
        return player == null ? null : player.getName();
    }

    @Override
    public String getIP(Object player) {
        return getIP((Connection) player);
    }

    /**
     * Get the ip of a connection.
     *
     * @param connection the connection
     * @return the ip or <code>null</code> if it's not connected through an ip address (e.g. a unix domain socket)
     */
    public static String getIP(Connection connection) {
        return connection.getSocketAddress() instanceof InetSocketAddress
                ? ((InetSocketAddress) connection.getSocketAddress()).getAddress().getHostAddress()
                : null;
    }

    @Override
    public String getInternUUID(Object player) {
        return player instanceof ProxiedPlayer ? ((ProxiedPlayer) player).getUniqueId().toString().replaceAll("-", "") : "none";
    }

    @Override
    public String getInternUUID(String player) {
        ProxiedPlayer proxiedPlayer = getPlayer(player);
        if (proxiedPlayer == null) {
            return null;
        }
        UUID uniqueId = proxiedPlayer.getUniqueId();
        return uniqueId == null ? null : uniqueId.toString().replaceAll("-", "");
    }

    @Override
    public boolean callChat(Object player) {
        Punishment pnt = PunishmentManager.get().getMute(UUIDManager.get().getUUID(getName(player)));
        if (pnt != null) {
            for (String str : pnt.getLayout()) {
                sendMessage(player, str);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean callCMD(Object player, String cmd) {
        Punishment pnt;
        if (Universal.get().isMuteCommand(cmd.substring(1))
                && (pnt = PunishmentManager.get().getMute(UUIDManager.get().getUUID(getName(player)))) != null) {
            for (String str : pnt.getLayout()) {
                sendMessage(player, str);
            }
            return true;
        }
        return false;
    }

    @Override
    public Object getMySQLFile() {
        return mysql;
    }

    @Override
    public Boolean getBoolean(Object file, String path) {
        return ((Configuration) file).getBoolean(path);
    }

    @Override
    public String getString(Object file, String path) {
        return ((Configuration) file).getString(path);
    }

    @Override
    public Long getLong(Object file, String path) {
        return ((Configuration) file).getLong(path);
    }

    @Override
    public Integer getInteger(Object file, String path) {
        return ((Configuration) file).getInt(path);
    }

    @Override
    public List<String> getStringList(Object file, String path) {
        return ((Configuration) file).getStringList(path);
    }

    @Override
    public boolean getBoolean(Object file, String path, boolean def) {
        return ((Configuration) file).getBoolean(path, def);
    }

    @Override
    public String getString(Object file, String path, String def) {
        return ((Configuration) file).getString(path, def);
    }

    @Override
    public long getLong(Object file, String path, long def) {
        return ((Configuration) file).getLong(path, def);
    }

    @Override
    public int getInteger(Object file, String path, int def) {
        return ((Configuration) file).getInt(path, def);
    }

    @Override
    public boolean contains(Object file, String path) {
        return ((Configuration) file).get(path) != null;
    }

    @Override
    public String getFileName(Object file) {
        return "[Only available on Bukkit-Version!]";
    }

    @Override
    public void callPunishmentEvent(Punishment punishment) {
        getPlugin().getProxy().getPluginManager().callEvent(new PunishmentEvent(punishment));
    }

    @Override
    public void callRevokePunishmentEvent(Punishment punishment, boolean massClear) {
        getPlugin().getProxy().getPluginManager().callEvent(new RevokePunishmentEvent(punishment, massClear));
    }

    @Override
    public boolean isOnlineMode() {
        return ProxyServer.getInstance().getConfig().isOnlineMode();
    }

    @Override
    public void notify(String perm, List<String> notification) {
        if (Universal.isRedis()) {
            notification.forEach((str) -> RedisBungeeHook.getApi().sendChannelMessage("advancedban:main", "notification " + perm + " " + str));
        } else {
            ProxyServer.getInstance().getPlayers()
                    .stream()
                    .filter((pp) -> (Universal.get().hasPerms(pp, perm)))
                    .forEachOrdered((pp) -> notification.forEach((str) -> sendMessage(pp, str)));
        }
    }

    @Override
    public void log(String msg) {
        ProxyServer.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(msg.replaceAll("&", "§")));
    }

    @Override
    public boolean isUnitTesting() {
        return false;
    }
}