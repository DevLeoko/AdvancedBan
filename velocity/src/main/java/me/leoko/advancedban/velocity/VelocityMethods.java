package me.leoko.advancedban.velocity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.utils.Permissionable;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.tabcompletion.TabCompleter;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import me.leoko.advancedban.velocity.event.PunishmentEvent;
import me.leoko.advancedban.velocity.listener.CommandReceiverVelocity;
import me.leoko.advancedban.velocity.utils.LuckPermsOfflineUser;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.simpleyaml.configuration.file.YamlFile;
import org.slf4j.Logger;

public class VelocityMethods implements MethodInterface {

    private final Logger logger;
    private final ProxyServer server;
    private final Path dataDirectory;

    private boolean luckPermsSupport;

    private final File configFile;
    private final File messageFile;
    private final File layoutFile;
    private final File mysqlFile;
    private YamlFile config;
    private YamlFile messages;
    private YamlFile layouts;
    private YamlFile mysql;




    public VelocityMethods(ProxyServer server, @DataDirectory Path dataDirectory, Logger logger){
        this.dataDirectory = dataDirectory;
        this.server = server;
        this.logger = logger;

        this.configFile = new File(getDataFolder(), "config.yml");
        this.messageFile =new File(getDataFolder(), "Messages.yml");
        this.layoutFile = new File(getDataFolder(), "Layouts.yml");
        this.mysqlFile = new File(getDataFolder(), "MySQL.yml");

        if (server.getPluginManager().getPlugin("luckperms").isPresent()) {
            luckPermsSupport = true;
            log("[AdvancedBan] Offline permission support through LuckPerms active");
        } else {
            luckPermsSupport = false;
            log("[AdvancedBan] No offline permission support through LuckPerms");
        }
    }




    @Override
    public void loadFiles() {
        try {
            config = new YamlFile(configFile);
            messages = new YamlFile(messageFile);
            layouts = new YamlFile(layoutFile);
            mysql = new YamlFile(mysqlFile);

            if (!configFile.exists()) {
                config.createNewFile();
            }
            if (!messageFile.exists()) {
                messages.createNewFile();
            }
            if (!layoutFile.exists()) {
                layouts.createNewFile();
            }
            if (!mysqlFile.exists()) {
                mysql.createNewFile();
            }
            config.load();
            messages.load();
            layouts.load();
            mysql.load();


        } catch (IOException e) {
            logger.error(e.getMessage());
        }


    }

    @Override
    public String getFromUrlJson(String url, String key) {
        try {
            HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();
            request.connect();

            JsonObject json = (JsonObject) JsonParser.parseReader(new InputStreamReader(request.getInputStream()));

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
        return "2.3.0";
    }

    @Override
    public String[] getKeys(Object file, String path) {
        return ((YamlFile) file).getConfigurationSection(path).getKeys(true).toArray(new String[0]);


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

    }

    @Override
    public boolean isBungee() {
        return true;
    }

    @Override
    public String clearFormatting(String s) {
        return s.replaceAll("(&[^\\s])+", "");
    }

    @Override
    public Object getPlugin() {
        return VelocityMain.get();
    }

    @Override
    public File getDataFolder() {
        return dataDirectory.toFile();
    }

    @Override
    public void setCommandExecutor(String cmd, String permission, TabCompleter tabCompleter) {

        //With permissions
        CommandMeta meta = server.getCommandManager().metaBuilder(cmd).build();
        server.getCommandManager().register(meta, new CommandReceiverVelocity(server, cmd));
    }

    @Override
    public void sendMessage(Object o, String s) {
        ((CommandSource) o).sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(s));
    }

    @Override
    public String getName(Object o) {
        if (o instanceof Player) {
            return ((Player) o).getUsername();
        } else {
            return "CONSOLE";
        }
    }

    @Override
    public String getName(String s) {
        return server.getPlayer(UUID.fromString(s)).get().getUsername();
    }

    @Override
    public String getIP(Object o) {
        return ((Player) o).getRemoteAddress().getHostName();
    }

    @Override
    public String getInternUUID(Object o) {
        return o instanceof  Player ? ((Player) o).getUniqueId().toString().replace("-", "") : "none";
    }

    @Override
    public String getInternUUID(String s) {
        Optional<Player> optionalPlayer = server.getPlayer(s);
        return optionalPlayer.map(value -> value.getUniqueId().toString().replace("-", "")).orElse(null);
    }

    @Override
    public boolean hasPerms(Object o, String s) {
        return ((CommandSource) o).hasPermission(s);
    }

    @Override
    public Permissionable getOfflinePermissionPlayer(String s) {
        if(luckPermsSupport) return new LuckPermsOfflineUser(s);
        return permission -> false;
    }

    @Override
    public boolean isOnline(String s) {
        return server.getPlayer(s).isPresent();
    }

    @Override
    public Object getPlayer(String s) {
        return server.getPlayer(s).orElse(null);
    }

    @Override
    public void kickPlayer(String s, String s1) {
        server.getPlayer(s).get().disconnect(LegacyComponentSerializer.legacyAmpersand().deserialize(s1));
    }

    @Override
    public Object[] getOnlinePlayers() {
        return server.getAllPlayers().toArray(new Player[]{});
    }

    @Override
    public void scheduleAsyncRep(Runnable runnable, long l, long l1) {
        server.getScheduler().buildTask(getPlugin(), runnable).delay(l*50, TimeUnit.MILLISECONDS).repeat(l1*50, TimeUnit.MILLISECONDS).schedule();
    }

    @Override
    public void scheduleAsync(Runnable runnable, long l) {
        server.getScheduler().buildTask(getPlugin(), runnable).delay(l*50, TimeUnit.MILLISECONDS).schedule();
    }

    @Override
    public void runAsync(Runnable runnable) {
        server.getScheduler().buildTask(getPlugin(), runnable).schedule();
    }

    @Override
    public void runSync(Runnable runnable) {
        runnable.run();
    }

    @Override
    public void executeCommand(String s) {
        server.getCommandManager().executeAsync(server.getConsoleCommandSource(), s).exceptionally((ex) -> {
            logger.error(ex.getMessage());
            return null;
        });
    }

    @Override
    public boolean callChat(Object o) {
        Punishment pnt = PunishmentManager.get().getMute(UUIDManager.get().getUUID(getName(o)));
        if (pnt != null) {
            for (String str : pnt.getLayout()) {
                sendMessage(o, str);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean callCMD(Object o, String s) {
        Punishment pnt;
        if (Universal.get().isMuteCommand(s.substring(1))
                && (pnt = PunishmentManager.get().getMute(UUIDManager.get().getUUID(getName(o)))) != null) {
            for (String str : pnt.getLayout()) {
                sendMessage(o, str);
            }
            return true;
        }
        return false;
    }

    @Override
    public Object getMySQLFile() {
        return null;
    }

    @Override
    public String parseJSON(InputStreamReader inputStreamReader, String s) {
        JsonObject element;
        try {
            element = JsonParser.parseReader(inputStreamReader).getAsJsonObject();
        } catch (IllegalStateException e) {
            return null;
        }
        JsonElement obj = element.get(s);
        return obj != null ? obj.toString().replaceAll("\"", "") : null;
    }

    @Override
    public String parseJSON(String s, String s1) {
        JsonObject element;
        try {
            element = (JsonObject) JsonParser.parseString(s).getAsJsonObject();
        } catch (IllegalStateException e) {
            return null;
        }
        JsonElement obj = element.get(s1);
        return obj != null ? obj.toString().replaceAll("\"", "") : null;
    }

    @Override
    public Boolean getBoolean(Object o, String s) {
        return ((YamlFile) o).getBoolean(s);
    }

    @Override
    public String getString(Object o, String s) {
        return ((YamlFile) o).getString(s);
    }

    @Override
    public Long getLong(Object o, String s) {
        return ((YamlFile) o).getLong(s);
    }

    @Override
    public Integer getInteger(Object o, String s) {
        return ((YamlFile) o).getInt(s);
    }

    @Override
    public List<String> getStringList(Object o, String s) {
        return ((YamlFile) o).getStringList(s);
    }

    @Override
    public boolean getBoolean(Object o, String s, boolean b) {
        return ((YamlFile) o).getBoolean(s, b);
    }

    @Override
    public String getString(Object o, String s, String s1) {
        return ((YamlFile) o).getString(s, s1);
    }

    @Override
    public long getLong(Object o, String s, long l) {
        return ((YamlFile) o).getLong(s, l);
    }

    @Override
    public int getInteger(Object o, String s, int i) {
        return ((YamlFile) o).getInt(s, i);
    }

    @Override
    public boolean contains(Object o, String s) {
        return ((YamlFile) o).contains(s);
    }

    @Override
    public String getFileName(Object o) {
        return "[Only available in the Bukkit Version!]";
    }

    @Override
    public void callPunishmentEvent(Punishment punishment) {
        server.getEventManager().fireAndForget(new PunishmentEvent(punishment));
    }

    @Override
    public void callRevokePunishmentEvent(Punishment punishment, boolean b) {
        server.getEventManager().fireAndForget(new PunishmentEvent(punishment, b));
    }

    @Override
    public boolean isOnlineMode() {
        return server.getConfiguration().isOnlineMode();
    }

    @Override
    public void notify(String s, List<String> list) {
        server.getAllPlayers().forEach(player -> {
            if (player.hasPermission(s)) {
                list.forEach(str -> sendMessage(player, str));
            }
        });
    }

    @Override
    public void log(String s) {
        server.getConsoleCommandSource().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(s));
    }

    @Override
    public boolean isUnitTesting() {
        return false;
    }
}
