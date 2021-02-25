package me.leoko.advancedban.bungee;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.bungee.event.PunishmentEvent;
import me.leoko.advancedban.bungee.event.RevokePunishmentEvent;
import me.leoko.advancedban.bungee.listener.CommandReceiverBungee;
import me.leoko.advancedban.bungee.utils.CloudNetCloudPermsOfflineUser;
import me.leoko.advancedban.bungee.utils.LuckPermsOfflineUser;
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
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.bstats.bungeecord.Metrics;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Leoko @ dev.skamps.eu on 23.07.2016.
 */
public class BungeeMethods implements MethodInterface {

    private final File configFile = new File(getDataFolder(), "config.yml");
    private final File messageFile = new File(getDataFolder(), "Messages.yml");
    private final File layoutFile = new File(getDataFolder(), "Layouts.yml");
    private final File mysqlFile = new File(getDataFolder(), "MySQL.yml");
    private Configuration config;
    private Configuration messages;
    private Configuration layouts;
    private Configuration mysql;

    private final Class<? extends Permissionable> permissionable;

    public BungeeMethods() {
        if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {
            permissionable = LuckPermsOfflineUser.class;

            log("[AdvancedBan] Offline permission support through LuckPerms active");
        } else if (ProxyServer.getInstance().getPluginManager().getPlugin("CloudNet-CloudPerms") != null) {
            permissionable = CloudNetCloudPermsOfflineUser.class;

            log("[AdvancedBan] Offline permission support through CloudNet-CloudPerms active");
        } else {
            permissionable = null;

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
    public String getFromUrlJson(String url, String key) {
        try {
            HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();
            request.connect();

            JsonParser jp = new JsonParser();
            JsonObject json = (JsonObject) jp.parse(new InputStreamReader(request.getInputStream()));

            String[] keys = key.split("\\|");
            for (int i = 0; i < keys.length - 1; i++) {
                json = json.getAsJsonObject(keys[i]);
            }

            return json.get(keys[keys.length - 1]).toString().replaceAll("\"", "");

        } catch (Exception exc) {
            return null;
        }
    }

    @Override
    public String getVersion() {
        return getPlugin().getDescription().getVersion();
    }

    @Override
    public String[] getKeys(Object file, String path) {
        //TODO not sure if it returns all keys or just the first :/
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
        Metrics metrics = new Metrics(getPlugin());
        metrics.addCustomChart(new Metrics.SimplePie("MySQL", () -> DatabaseManager.get().isUseMySQL() ? "yes" : "no"));
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
    public void setCommandExecutor(String cmd, TabCompleter tabCompleter) {
        ProxyServer.getInstance().getPluginManager().registerCommand(getPlugin(), new CommandReceiverBungee(cmd));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void sendMessage(Object player, String msg) {
        ((CommandSender) player).sendMessage(msg);
    }

    @Override
    public boolean hasPerms(Object player, String perms) {
        return player != null && ((CommandSender) player).hasPermission(perms);
    }

    @Override
    public Permissionable getOfflinePermissionPlayer(String name) {
        if (permissionable != null) {
            try {
                return permissionable.getConstructor(String.class).newInstance(name);
            } catch (Exception ignored) {}
        }

        return permission -> false;
    }

    @Override
    public boolean isOnline(String name) {
        try {
            if (Universal.isRedis()) {
                for (String str : RedisBungee.getApi().getHumanPlayersOnline()) {
                    if (str.equalsIgnoreCase(name)) {
                        return RedisBungee.getApi().getPlayerIp(RedisBungee.getApi().getUuidFromName(str)) != null;
                    }
                }
            }
            return getPlayer(name).getAddress() != null;
        } catch (NullPointerException exc) {
            return false;
        }
    }

    @Override
    public ProxiedPlayer getPlayer(String name) {
        return ProxyServer.getInstance().getPlayer(name);
    }


    @Override
    public void kickPlayer(String player, String reason) {
        if(BungeeMain.getCloudSupport() != null){
            BungeeMain.getCloudSupport().kick(getPlayer(player).getUniqueId(), reason);
        }else if (Universal.isRedis()) {
            RedisBungee.getApi().sendChannelMessage("advancedban:main", "kick " + player + " " + reason);
        } else {
            getPlayer(player).disconnect(TextComponent.fromLegacyText(reason));
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
        return ProxyServer.getInstance().getPlayer(UUID.fromString(uuid)).getName();
    }

    @Override
    public String getIP(Object player) {
        return ((ProxiedPlayer) player).getAddress().getHostName();
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
    public String parseJSON(InputStreamReader json, String key) {
        JsonElement element = new JsonParser().parse(json);
        if (element instanceof JsonNull) {
            return null;
        }
        JsonElement obj = ((JsonObject) element).get(key);
        return obj != null ? obj.toString().replaceAll("\"", "") : null;
    }

    @Override
    public String parseJSON(String json, String key) {
        JsonElement element = new JsonParser().parse(json);
        if (element instanceof JsonNull) {
            return null;
        }
        JsonElement obj = ((JsonObject) element).get(key);
        return obj != null ? obj.toString().replaceAll("\"", "") : null;
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
            notification.forEach((str) -> RedisBungee.getApi().sendChannelMessage("advancedban:main", "notification " + perm + " " + str));
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