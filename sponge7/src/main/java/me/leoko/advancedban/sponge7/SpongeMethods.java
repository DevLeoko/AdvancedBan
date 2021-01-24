package me.leoko.advancedban.sponge7;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.leoko.advancedban.*;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.sponge7.event.PunishmentEvent;
import me.leoko.advancedban.sponge7.event.RevokePunishmentEvent;
import me.leoko.advancedban.sponge7.listener.CommandReceiverSponge;
import me.leoko.advancedban.sponge7.utils.LuckPermsOfflineUser;
import me.leoko.advancedban.utils.Permissionable;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.tabcompletion.TabCompleter;
import ninja.leaping.configurate.ConfigurationNode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SpongeMethods extends AbstractConfigurateMethodInterface {

    private final Game game;
    private final Path dataDirectory;
    private final Logger logger;

    private boolean luckPermsSupport;
    public SpongeMethods(Path dataDirectory,
                           final @NonNull Game game,
                           Logger logger) {
        super(dataDirectory);
        this.dataDirectory = dataDirectory;
        this.game = game;
        this.logger = logger;

        if (Sponge.getPluginManager().isLoaded("luckperms")) {
            luckPermsSupport = true;
            log("[AdvancedBan] Offline permission support through LuckPerms active");
        } else {
            luckPermsSupport = false;
            log("[AdvancedBan] No offline permission support through LuckPerms");
        }

    }


    @Override
    public String getFromUrlJson(String url, String key) {
        try {
            HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();
            request.connect();

            JsonObject json = (JsonObject) new JsonParser().parse(new InputStreamReader(request.getInputStream()));

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
        return VersionInfo.VERSION;
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
        return ServerType.SPONGE;
    }

    @Override
    public String clearFormatting(String text) {
        return text.replaceAll("(&[^\\s])+", "");
    }

    @Override
    public Object getPlugin() {
        return SpongeMain.get();
    }

    @Override
    public File getDataFolder() {
        return dataDirectory.toFile();
    }

    @Override
    public void setCommandExecutor(String cmd, TabCompleter tabCompleter) {
        game.getCommandManager().register(SpongeMain.get().getPluginContainer(), new CommandReceiverSponge(this.game, cmd), cmd);
    }

    @Override
    public void sendMessage(Object player, String msg) {
        ((CommandSource) player).sendMessage(TextSerializers.FORMATTING_CODE.deserialize(msg));
    }

    @Override
    public String getName(Object player) {
        if (player instanceof Player) {
            return ((Player) player).getName();
        } else {
            return "CONSOLE";
        }
    }

    @Override
    public String getName(String uuid) {
        return getServer().getPlayer(UUID.fromString(uuid)).get().getName();
    }

    @Override
    public String getIP(Object player) {
        return ((Player)player).getConnection().getAddress().getHostName();
    }

    @Override
    public String getInternUUID(Object player) {
        return player instanceof Player ? ((Player) player).getUniqueId().toString().replace("-", "") : "none";
    }

    @Override
    public String getInternUUID(String player) {
        Optional<Player> optionalPlayer = getServer().getPlayer(player);
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
        return getServer().getPlayer(name).isPresent();
    }

    @Override
    public Player getPlayer(String name) {
        return getServer().getPlayer(name).orElse(null);
    }

    @Override
    public void kickPlayer(String player, String reason) {
        getServer().getPlayer(player).get().kick(TextSerializers.FORMATTING_CODE.deserialize(reason));
    }

    @Override
    public Player[] getOnlinePlayers() {
        return getServer().getOnlinePlayers().toArray(new Player[]{});
    }

    @Override
    public void scheduleAsyncRep(Runnable rn, long l1, long l2) {
        Sponge.getScheduler().createTaskBuilder()
                .async()
                .execute(rn)
                .delay(l1*50, TimeUnit.MILLISECONDS)
                .interval(l2*50, TimeUnit.MILLISECONDS)
                .submit(SpongeMain.get().getPluginContainer());
    }

    @Override
    public void scheduleAsync(Runnable rn, long l1) {
        Sponge.getScheduler().createTaskBuilder()
                .async()
                .execute(rn)
                .delay(l1*50, TimeUnit.MILLISECONDS)
                .submit(SpongeMain.get().getPluginContainer());
    }

    @Override
    public void runAsync(Runnable rn) {
        Sponge.getScheduler().createTaskBuilder()
                .async()
                .execute(rn)
                .submit(SpongeMain.get().getPluginContainer());
    }

    @Override
    public void runSync(Runnable rn) {
        rn.run();
    }

    @Override
    public void executeCommand(String cmd) {
        game.getCommandManager().process(game.getServer().getConsole().getCommandSource().get(), cmd);
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
        JsonObject element;
        try {
            element = new JsonParser().parse(json).getAsJsonObject();
        } catch (IllegalStateException e) {
            return null;
        }
        JsonElement obj = element.get(key);
        return obj != null ? obj.toString().replaceAll("\"", "") : null;
    }

    @Override
    public String parseJSON(String json, String key) {
        JsonObject element;
        try {
            element = new JsonParser().parse(json).getAsJsonObject();
        } catch (IllegalStateException e) {
            return null;
        }
        JsonElement obj = element.get(key);
        return obj != null ? obj.toString().replaceAll("\"", "") : null;
    }

    @Override
    public String getFileName(Object file) {
        return "[Only Available in the Bukkit Version!]";
    }

    @Override
    public void callPunishmentEvent(Punishment punishment) {
        game.getEventManager().post(new PunishmentEvent(punishment));
    }

    @Override
    public void callRevokePunishmentEvent(Punishment punishment, boolean massClear) {
        game.getEventManager().post(new RevokePunishmentEvent(punishment, massClear));
    }

    @Override
    public boolean isOnlineMode() {
        return getServer().getOnlineMode();
    }

    @Override
    public void notify(String perm, List<String> notification) {
        getServer().getOnlinePlayers().forEach(player -> {
            if (player.hasPermission(perm)) {
                notification.forEach(str -> sendMessage(player, str));
            }
        });
    }

    @Override
    public void log(String msg) {
        logger.info(msg);
    }

    @Override
    public boolean isUnitTesting() {
        return false;
    }



    private Server getServer() {
        return this.game.getServer();
    }

}
