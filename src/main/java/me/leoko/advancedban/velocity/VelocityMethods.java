package me.leoko.advancedban.velocity;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import de.leonhard.storage.LightningBuilder;
import de.leonhard.storage.Yaml;
import de.leonhard.storage.internal.settings.ConfigSettings;
import de.leonhard.storage.internal.settings.ReloadSettings;
import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.tabcompletion.TabCompleter;
import me.leoko.advancedban.velocity.event.PunishmentEvent;
import me.leoko.advancedban.velocity.event.RevokePunishmentEvent;
import me.leoko.advancedban.velocity.listener.CommandReceiverVelocity;
import net.kyori.text.TextComponent;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class VelocityMethods implements MethodInterface {

    private final File configFile = new File(getDataFolder(), "config.yml");
    private final File messageFile = new File(getDataFolder(), "Messages.yml");
    private final File layoutFile = new File(getDataFolder(), "Layouts.yml");
    private final File mysqlFile = new File(getDataFolder(), "MySQL.yml");
    private Yaml config;
    private Yaml messages;
    private Yaml layouts;
    private Yaml mysql;

    @Override
    public void loadFiles() {
        config = LightningBuilder.fromFile(configFile).addInputStreamFromResource(configFile.getName()).setConfigSettings(ConfigSettings.PRESERVE_COMMENTS).createYaml();
        messages = LightningBuilder.fromFile(messageFile).addInputStreamFromResource(messageFile.getName()).setConfigSettings(ConfigSettings.PRESERVE_COMMENTS).createYaml();
        layouts = LightningBuilder.fromFile(layoutFile).addInputStreamFromResource(layoutFile.getName()).setConfigSettings(ConfigSettings.PRESERVE_COMMENTS).createYaml();
        mysql = LightningBuilder.fromFile(mysqlFile).setConfigSettings(ConfigSettings.PRESERVE_COMMENTS).createYaml();
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
        return VelocityMain.VelocityPluginInfo.PLUGIN_VERSION;
    }

    @Override
    public String[] getKeys(Object file, String path) {
        return ((Yaml) file).getSection(path).singleLayerKeySet().toArray(new String[0]);
    }

    @Override
    public Yaml getConfig() {
        return config;
    }

    @Override
    public Yaml getMessages() {
        return messages;
    }

    @Override
    public Yaml getLayouts() {
        return layouts;
    }

    @Override
    public void setupMetrics() {
        // Velocity does not support bStats
    }

    @Override
    public VelocityMain getPlugin() {
        return VelocityMain.get();
    }

    @Override
    public File getDataFolder() {
        return getPlugin().folder;
    }

    @Override
    public void setCommandExecutor(String cmd, TabCompleter tabCompleter) {
        getPlugin().server.getCommandManager().register(new CommandReceiverVelocity(cmd), cmd);
    }

    @Override
    public void sendMessage(Object player, String msg) {
        if (player instanceof Player) {
            ((Player) player).sendMessage(TextComponent.of(msg));
        } else {
            ((CommandSource) player).sendMessage(TextComponent.of(msg));
        }
    }

    @Override
    public String getName(Object player) {
        if (player instanceof Player) {
            return ((Player) player).getUsername();
        } else {
            return "CONSOLE";
        }
    }

    @Override
    public String getName(String uuid) {
        return getPlugin().server.getPlayer(uuid).get().getUsername();
    }

    @Override
    public String getIP(Object player) {
        return ((Player)player).getRemoteAddress().getAddress().getHostAddress();
    }

    @Override
    public String getInternUUID(Object player) {
        return player instanceof  Player ? ((Player) player).getUniqueId().toString().replace("-", "") : "none";
    }

    @Override
    public String getInternUUID(String player) {
        Optional<Player> optionalPlayer = getPlugin().server.getPlayer(player);
        return optionalPlayer.map(value -> value.getUniqueId().toString().replace("-", "")).orElse(null);
    }

    @Override
    public boolean hasPerms(Object player, String perms) {
        if (player instanceof Player) {
            return ((Player) player).hasPermission(perms);
        } else {
            return ((CommandSource) player).hasPermission(perms);
        }
    }

    @Override
    public boolean hasOfflinePerms(String name, String perms) {
        return false;
    }

    @Override
    public boolean isOnline(String name) {
        return getPlugin().server.getPlayer(name).isPresent();
    }

    @Override
    public Object getPlayer(String name) {
        return getPlugin().server.getPlayer(name).get();
    }

    @Override
    public void kickPlayer(String player, String reason) {
        getPlugin().server.getPlayer(player).get().disconnect(TextComponent.of(reason));
    }

    @Override
    public Player[] getOnlinePlayers() {
        return getPlugin().server.getAllPlayers().toArray(new Player[0]);
    }

    @Override
    public void scheduleAsyncRep(Runnable rn, long l1, long l2) {
        getPlugin().server.getScheduler().buildTask(getPlugin(), rn).repeat(l2*50, TimeUnit.SECONDS);
    }

    @Override
    public void scheduleAsync(Runnable rn, long l1) {
        getPlugin().server.getScheduler().buildTask(getPlugin(), rn).schedule();
    }

    @Override
    public void runAsync(Runnable rn) {
        getPlugin().server.getScheduler().buildTask(getPlugin(), rn).schedule();
    }

    @Override
    public void runSync(Runnable rn) {
        rn.run();
    }

    @Override
    public void executeCommand(String cmd) {
        getPlugin().server.getCommandManager().execute(getPlugin().server.getConsoleCommandSource(), cmd);
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
        return ((Yaml) file).getBoolean(path);
    }

    @Override
    public String getString(Object file, String path) {
        return ((Yaml) file).getString(path);
    }

    @Override
    public Long getLong(Object file, String path) {
        return ((Yaml) file).getLong(path);
    }

    @Override
    public Integer getInteger(Object file, String path) {
        return ((Yaml) file).getInt(path);
    }

    @Override
    public List<String> getStringList(Object file, String path) {
        return ((Yaml) file).getStringList(path);
    }

    @Override
    public boolean getBoolean(Object file, String path, boolean def) {
        return ((Yaml) file).getOrDefault(path, def);
    }

    @Override
    public String getString(Object file, String path, String def) {
        return ((Yaml) file).getOrDefault(path, def);
    }

    @Override
    public long getLong(Object file, String path, long def) {
        return ((Yaml) file).getOrDefault(path, def);
    }

    @Override
    public int getInteger(Object file, String path, int def) {
        return ((Yaml) file).getOrDefault(path, def);
    }

    @Override
    public boolean contains(Object file, String path) {
        return ((Yaml) file).contains(path);
    }

    @Override
    public String getFileName(Object file) {
        return ((Yaml) file).getName();
    }

    @Override
    public void callPunishmentEvent(Punishment punishment) {
        getPlugin().server.getEventManager().fire(new PunishmentEvent(punishment));
    }

    @Override
    public void callRevokePunishmentEvent(Punishment punishment, boolean massClear) {
        getPlugin().server.getEventManager().fire(new RevokePunishmentEvent(punishment, massClear));
    }

    @Override
    public boolean isOnlineMode() {
        return getPlugin().server.getConfiguration().isOnlineMode();
    }

    @Override
    public void notify(String perm, List<String> notification) {
        getPlugin().server.getAllPlayers().forEach(player -> notification.forEach(str -> sendMessage(player,str)));
    }

    @Override
    public void log(String msg) {
        getPlugin().server.getConsoleCommandSource().sendMessage(TextComponent.of(msg.replaceAll("&", "§")));
    }

    @Override
    public boolean isUnitTesting() {
        return false;
    }

    private InputStream getResource(String resource) {
        return getClass().getClassLoader().getResourceAsStream(resource);
    }
}
