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
import me.leoko.advancedban.manager.DatabaseManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.utils.Punishment;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
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
    private Configuration config;
    private Configuration messages;
    private Configuration layouts;
    private Configuration mysql;

    @Override
    public void loadFiles() {
        try {
            if (!getDataFolder().exists()) {
                //noinspection ResultOfMethodCallIgnored
                getDataFolder().mkdirs();
            }
            if (!configFile.exists()) {
                Files.copy(((Plugin) getPlugin()).getResourceAsStream("config.yml"), configFile.toPath());
            }
            if (!messageFile.exists()) {
                Files.copy(((Plugin) getPlugin()).getResourceAsStream("Messages.yml"), messageFile.toPath());
            }
            if (!layoutFile.exists()) {
                Files.copy(((Plugin) getPlugin()).getResourceAsStream("Layouts.yml"), layoutFile.toPath());
            }

            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            messages = ConfigurationProvider.getProvider(YamlConfiguration.class).load(messageFile);
            layouts = ConfigurationProvider.getProvider(YamlConfiguration.class).load(layoutFile);
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
        return ((Plugin) getPlugin()).getDescription().getVersion();
    }

    @Override
    public String[] getKeys(Object file, String path) {
        //TODO not sure if it returns all keys or just the first :/
        return ((Configuration) file).getSection(path).getKeys().toArray(new String[0]);
    }

    @Override
    public Object getConfig() {
        return config;
    }

    @Override
    public Object getMessages() {
        return messages;
    }

    @Override
    public Object getLayouts() {
        return layouts;
    }

    @Override
    public void setupMetrics() {
        Metrics metrics = new Metrics((Plugin) getPlugin());
        metrics.addCustomChart(new Metrics.SimplePie("MySQL", () -> DatabaseManager.get().isUseMySQL() ? "yes" : "no"));
    }

    @Override
    public Object getPlugin() {
        return BungeeMain.get();
    }

    @Override
    public File getDataFolder() {
        return ((Plugin) getPlugin()).getDataFolder();
    }

    @Override
    public void setCommandExecutor(String cmd) {
        ProxyServer.getInstance().getPluginManager().registerCommand((Plugin) getPlugin(), new CommandReceiverBungee(cmd));
    }

    @Override
    public void sendMessage(Object player, String msg) {
        ((CommandSender) player).sendMessage(msg);
    }

    @Override
    public boolean hasPerms(Object player, String perms) {
        try {
            return ((CommandSender) player).hasPermission(perms);
        } catch (NullPointerException ex) {
            return false;
        }
    }

    @Override
    public boolean isOnline(String name) {
        try {
            if (Universal.get().useRedis()) {
                for (String str : RedisBungee.getApi().getHumanPlayersOnline()) {
                    if (str.equalsIgnoreCase(name)) {
                        return RedisBungee.getApi().getPlayerIp(RedisBungee.getApi().getUuidFromName(str)) != null;
                    }
                }
            }
            return ProxyServer.getInstance().getPlayer(name).getAddress() != null;
        } catch (NullPointerException exc) {
            return false;
        }
    }

    @Override
    public Object getPlayer(String name) {
        return ProxyServer.getInstance().getPlayer(name);
    }

    @Override
    public void kickPlayer(Object player, String reason) {
        ((ProxiedPlayer) player).disconnect(reason);
    }

    @Override
    public Object[] getOnlinePlayers() {
        return ProxyServer.getInstance().getPlayers().toArray();
    }

    @Override
    public void scheduleAsyncRep(Runnable rn, long l1, long l2) {
        ProxyServer.getInstance().getScheduler().schedule((Plugin) getPlugin(), rn, l1 * 50, l2 * 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void scheduleAsync(Runnable rn, long l1) {
        ProxyServer.getInstance().getScheduler().schedule((Plugin) getPlugin(), rn, l1 * 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void runAsync(Runnable rn) {
        ProxyServer.getInstance().getScheduler().runAsync((Plugin) getPlugin(), rn);
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
        ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(player);
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
        if (Universal.get().isMuteCommand(cmd.split(" ")[0].substring(1))
                && (pnt = PunishmentManager.get().getMute(UUIDManager.get().getUUID(getName(player)))) != null) {
            for (String str : pnt.getLayout()) {
                sendMessage(player, str);
            }
            return true;
        }
        return false;
    }

    @Override
    public void loadMySQLFile(File file) {
        try {
            mysql = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createMySQLFile(File file) {
        mysql.set("MySQL.IP", "localhost");
        mysql.set("MySQL.DB-Name", "YourDatabase");
        mysql.set("MySQL.Username", "root");
        mysql.set("MySQL.Password", "pw123");
        mysql.set("MySQL.Port", 3306);
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(mysql, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        ((Plugin) getPlugin()).getProxy().getPluginManager().callEvent(new PunishmentEvent(punishment));
    }

    @Override
    public void callRevokePunishmentEvent(Punishment punishment, boolean massClear) {
        ((Plugin) getPlugin()).getProxy().getPluginManager().callEvent(new RevokePunishmentEvent(punishment, massClear));
    }

    @Override
    public boolean isOnlineMode() {
        return ProxyServer.getInstance().getConfig().isOnlineMode();
    }

    @Override
    public void notify(String perm, List<String> notification) {
        if (Universal.get().useRedis()) {
            RedisBungee.getApi().sendChannelMessage("AdvancedBan", "notificationperm " + perm);
            notification.forEach((str) -> {
                RedisBungee.getApi().sendChannelMessage("AdvancedBan", "notification " + str);
            });
        } else {
            ProxyServer.getInstance().getPlayers().stream().filter((pp) -> (hasPerms(pp, perm))).forEachOrdered((pp) -> {
                notification.forEach((str) -> {
                    sendMessage(pp, str);
                });
            });
        }
    }
}
