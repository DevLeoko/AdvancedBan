package me.leoko.advancedban.velocity;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.leoko.advancedban.AbstractMethodInterface;
import me.leoko.advancedban.ServerType;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.utils.Permissionable;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.tabcompletion.TabCompleter;
import me.leoko.advancedban.velocity.command.CommandReceiverVelocity;
import me.leoko.advancedban.velocity.event.PunishmentEvent;
import me.leoko.advancedban.velocity.event.RevokePunishmentEvent;
import me.leoko.advancedban.velocity.utils.LuckPermsOfflineUser;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

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

public class VelocityMethods extends AbstractMethodInterface<ConfigurationNode> {

  private final ProxyServer server;
  private final Path dataDirectory;
  private final Logger logger;

  private boolean luckPermsSupport;

  public VelocityMethods(Path dataDirectory,
                         final @NonNull ProxyServer server,
                         Logger logger) {
    super(dataDirectory);
    this.dataDirectory = dataDirectory;
    this.server = server;
    this.logger = logger;

    if (server.getPluginManager().getPlugin("luckperms").isPresent()) {
      luckPermsSupport = true;
      log("[AdvancedBan] Offline permission support through LuckPerms active");
    } else {
      luckPermsSupport = false;
      log("[AdvancedBan] No offline permission support through LuckPerms");
    }

  }


  @Override
  protected ConfigurationNode loadConfiguration(Path configPath) throws IOException {
      YAMLConfigurationLoader loader = YAMLConfigurationLoader.builder().setPath(configPath).build();
      return loader.load();
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
    return "@version";
  }

  // Object.toString() is considered safe in this scenario (as per Configurate Docs)
  @Override
  public String[] getKeys(Object file, String path) {
    return ((ConfigurationNode)file).getNode(path.split("\\.")).getChildrenMap().keySet().stream().map(Object::toString).toArray(String[]::new);
  }

  @Override
  public void setupMetrics() {
    // no bstats for velocity yet
  }


  @Override @Deprecated
  public boolean isBungee() {
    return false;
  }

  @Override
  public boolean isProxy() {
    return true;
  }

  @Override
  public ServerType getServerType() {
    return ServerType.VELOCITY;
  }

  @Override
  public String clearFormatting(String text) {
    return text.replaceAll("(&[^\\s])+", "");
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
  public void setCommandExecutor(String cmd, TabCompleter tabCompleter) {
    CommandMeta meta = server.getCommandManager().metaBuilder(cmd).build();
    server.getCommandManager().register(meta, new CommandReceiverVelocity(server, cmd));
  }

  @Override
  public void sendMessage(Object player, String msg) {
      ((CommandSource) player).sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(msg));
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
      return server.getPlayer(UUID.fromString(uuid)).get().getUsername();
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
    Optional<Player> optionalPlayer = server.getPlayer(player);
    return optionalPlayer.map(value -> value.getUniqueId().toString().replace("-", "")).orElse(null);
  }

  @Override
  public boolean hasPerms(Object player, String perms) {
      return ((CommandSource) player).hasPermission(perms);
  }

  @Override
  public Permissionable getOfflinePermissionPlayer(String name) {
    if(luckPermsSupport) return new LuckPermsOfflineUser(name);

    return permission -> false;
  }

  @Override
  public boolean isOnline(String name) {
    return server.getPlayer(name).isPresent();
  }

  @Override
  public Player getPlayer(String name) {
    if(server.getPlayer(name).isPresent()) {
      return server.getPlayer(name).orElse(null);
    }
    return null;
  }

  @Override
  public void kickPlayer(String player, String reason) {
    server.getPlayer(player).get().disconnect(LegacyComponentSerializer.legacyAmpersand().deserialize(reason));
  }

  @Override
  public Player[] getOnlinePlayers() {
    return server.getAllPlayers().toArray(new Player[]{});
  }

  @Override
  public void scheduleAsyncRep(Runnable rn, long l1, long l2) {
    server.getScheduler().buildTask(getPlugin(), rn).delay(l1*50, TimeUnit.MILLISECONDS).repeat(l2*50, TimeUnit.MILLISECONDS);
  }

  @Override
  public void scheduleAsync(Runnable rn, long l1) {
    server.getScheduler().buildTask(getPlugin(), rn).delay(l1*50, TimeUnit.MILLISECONDS).schedule();
  }

  @Override
  public void runAsync(Runnable rn) {
    server.getScheduler().buildTask(getPlugin(), rn).schedule();
  }

  @Override
  public void runSync(Runnable rn) {
    rn.run();
  }

  @Override
  public void executeCommand(String cmd) {
    server.getCommandManager().executeImmediatelyAsync(server.getConsoleCommandSource(), cmd);
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
    return ((ConfigurationNode)file).getNode(path).getBoolean();
  }

  @Override
  public String getString(Object file, String path) {
    return ((ConfigurationNode)file).getNode(path.split("\\.")).getString();
  }

  @Override
  public Long getLong(Object file, String path) {
    return ((ConfigurationNode)file).getNode(path.split("\\.")).getLong();
  }

  @Override
  public Integer getInteger(Object file, String path) {
    return ((ConfigurationNode)file).getNode(path.split("\\.")).getInt();
  }

  @Override
  public List<String> getStringList(Object file, String path) {
    try {
      return ((ConfigurationNode)file).getNode(path.split("\\.")).getList(TypeToken.of(String.class));
    } catch (ObjectMappingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean getBoolean(Object file, String path, boolean def) {
    return ((ConfigurationNode)file).getNode(path.split("\\.")).getBoolean(def);
  }

  @Override
  public String getString(Object file, String path, String def) {
    return ((ConfigurationNode)file).getNode(path.split("\\.")).getString(def);
  }

  @Override
  public long getLong(Object file, String path, long def) {
    return ((ConfigurationNode)file).getNode(path.split("\\.")).getLong(def);
  }

  @Override
  public int getInteger(Object file, String path, int def) {
    return ((ConfigurationNode)file).getNode(path.split("\\.")).getInt(def);
  }

  @Override
  public boolean contains(Object file, String path) {
    return (!((ConfigurationNode)file).getNode(path.split("\\.")).isEmpty());
  }

  @Override
  public String getFileName(Object file) {
    return "[Only Available in the Bukkit Version!]";
  }

  @Override
  public void callPunishmentEvent(Punishment punishment) {
    server.getEventManager().fire(new PunishmentEvent(punishment));
  }

  @Override
  public void callRevokePunishmentEvent(Punishment punishment, boolean massClear) {
    server.getEventManager().fire(new RevokePunishmentEvent(punishment, massClear));
  }

  @Override
  public boolean isOnlineMode() {
    return server.getConfiguration().isOnlineMode();
  }

  @Override
  public void notify(String perm, List<String> notification) {
    server.getAllPlayers().forEach(player -> {
      if(player.hasPermission(perm)) notification.forEach(str -> sendMessage(player, str));});
  }

  @Override
  public void log(String msg) {
    logger.info(msg);
  }

  @Override
  public boolean isUnitTesting() {
    return false;
  }
}
