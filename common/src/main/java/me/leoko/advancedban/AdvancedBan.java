package me.leoko.advancedban;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.AccessLevel;
import lombok.Getter;
import me.leoko.advancedban.command.AbstractCommand;
import me.leoko.advancedban.configuration.Configuration;
import me.leoko.advancedban.configuration.Layouts;
import me.leoko.advancedban.configuration.Messages;
import me.leoko.advancedban.configuration.MySQLConfiguration;
import me.leoko.advancedban.manager.*;
import me.leoko.advancedban.punishment.InterimData;
import me.leoko.advancedban.punishment.Punishment;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;

/**
 * @author SupremeMortal
 */
@Getter
public abstract class AdvancedBan {
    @Getter(value = AccessLevel.NONE)
    public static final YAMLMapper YAML_MAPPER = (YAMLMapper) new YAMLMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    @Getter(value = AccessLevel.NONE)
    public static final ObjectMapper JSON_MAPPER = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    @Getter(value = AccessLevel.NONE)
    private static AdvancedBan instance;

    private final Map<Object, AdvancedBanPlayer> players = Collections.synchronizedMap(new HashMap<>());
    @Getter(value = AccessLevel.NONE)
    private final Map<Object, InetAddress> addresses = Collections.synchronizedMap(new HashMap<>());
    private final UUIDManager.FetcherMode mode;
    private final boolean mojangAuthed;
    private final CommandManager commandManager = new CommandManager(this);
    private final PunishmentManager punishmentManager = new PunishmentManager(this);
    private final DatabaseManager databaseManager = new DatabaseManager(this);
    private final UUIDManager uuidManager = new UUIDManager(this);
    private final MessageManager messageManager = new MessageManager(this);
    private final TimeManager timeManager = new TimeManager(this);
    private final UpdateManager updateManager = new UpdateManager(this);
    private final AdvancedBanLogger logger = new AdvancedBanLogger(this);
    private final Set<String> commands = new HashSet<>();
    private Configuration configuration;
    private Layouts layouts;
    private Messages messages;
    @Getter(value = AccessLevel.NONE)
    private MySQLConfiguration mySQLConfiguration = null;

    protected AdvancedBan(UUIDManager.FetcherMode mode, boolean mojangAuthed) {
        if (instance != null) {
            throw new IllegalStateException("AdvancedBan has already been initialized");
        }
        instance = this;
        this.mode = mode;
        this.mojangAuthed = mojangAuthed;
    }

    public static AdvancedBan get() {
        return instance;
    }

    public final void onEnable() {
        try {
            loadFiles();
        } catch (IOException e) {
            throw new RuntimeException("Unable to load configuration files", e);
        }
        logger.onEnable();
        databaseManager.onEnable();
        updateManager.onEnable();
        uuidManager.onEnable();
        punishmentManager.onEnable();
        commandManager.onEnable();
    }

    public final void onDisable() {

    }

    public final void loadFiles() throws IOException {
        Path dataPath = getDataFolderPath();
        Files.createDirectories(dataPath);
        Path configPath = checkExists("config.yml");
        configuration = Configuration.load(configPath);
        Path layoutsPath = checkExists("Layouts.yml");
        layouts = Layouts.load(layoutsPath);
        Path messagesPath = checkExists("Messages.yml");
        messages = Messages.load(messagesPath);
        if (configuration.isUsingMySQL()) {
            Path mysqlPath = checkExists("MySQL.yml");
            mySQLConfiguration = MySQLConfiguration.load(mysqlPath);
        }
    }

    private Path checkExists(String file) throws IOException {
        Path filePath = getDataFolderPath().resolve(file);
        InputStream resource = AdvancedBan.class.getClassLoader().getResourceAsStream(file);
        if (resource == null) {
            throw new IllegalStateException("Resource was not found in JAR");
        }
        if (Files.notExists(filePath) || !Files.isRegularFile(filePath)) {
            Files.deleteIfExists(filePath);
            Files.createFile(filePath);
            Files.copy(resource, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        return filePath;
    }

    public Optional<MySQLConfiguration> getMySQLConfiguration() {
        return Optional.ofNullable(mySQLConfiguration);
    }

    public boolean isOnline(String name) {
        return getPlayer(name).isPresent();
    }

    public boolean isOnline(UUID uuid) {
        return getPlayer(uuid).isPresent();
    }

    public void notify(String permission, Collection<String> notifications) {
        for (AdvancedBanPlayer player : getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                notifications.forEach(player::sendMessage);
            }
        }
    }

    public Optional<String> onPreLogin(String name, UUID uuid, InetAddress address) {
        InterimData interimData = punishmentManager.load(uuid, name, address);

        Optional<Punishment> punishment = interimData.getBan();

        if (!punishment.isPresent()) {
            interimData.accept(punishmentManager);
            addresses.put(name, address);
            addresses.put(uuid, address);
        }

        return punishment.map(Punishment::getLayoutBSN);
    }

    public void onLogin(AdvancedBanPlayer player) {
        registerPlayer(player);
    }

    protected final void registerPlayer(AdvancedBanPlayer player) {
        players.put(player.getUniqueId(), player);
        players.put(player.getName().toLowerCase(), player);
        players.put(player.getAddress(), player);
    }

    public void onDisconnect(AdvancedBanPlayer player) {
        removePlayer(player);
        addresses.remove(player.getName());
        addresses.remove(player.getUniqueId());
    }

    protected void removePlayer(AdvancedBanPlayer player) {
        players.remove(player.getUniqueId());
        players.remove(player.getName());
        players.remove(player.getAddress());
        punishmentManager.discard(player);
    }

    public boolean onChat(AdvancedBanPlayer player, String message) {
        Optional<List<String>> layout = punishmentManager.getMute(player.getUniqueId()).map(Punishment::getLayout);
        if (layout.isPresent()) {
            layout.get().forEach(player::sendMessage);
            return true;
        }
        return false;
    }

    public boolean onCommand(AdvancedBanPlayer player, String command) {
        Optional<List<String>> layout = punishmentManager.getMute(player.getUniqueId()).map(Punishment::getLayout);
        if (layout.isPresent() && isMutedCommand(command)) {
            layout.get().forEach(player::sendMessage);
            return true;
        }
        return false;
    }

    public boolean isAdvancedBanCommand(String command) {
        command = command.split(" ")[0];

        return commands.contains(command);
    }

    public boolean isMutedCommand(String command) {
        command = command.split(" ")[0];

        for (String mutedCommand : configuration.getMuteCommands()) {
            if (mutedCommand.equalsIgnoreCase(command)) {
                return true;
            }
        }
        return false;
    }

    public final void registerCommand(AbstractCommand command) {
        commands.add(command.getName());
        onRegisterCommand(command);
    }

    public Optional<AdvancedBanPlayer> getPlayer(UUID uuid) {
        return Optional.ofNullable(players.get(uuid));
    }

    public Optional<AdvancedBanPlayer> getPlayer(String val) {
        Optional<AdvancedBanPlayer> player = Optional.ofNullable(players.get(val.toLowerCase()));

        if (!player.isPresent()) {
            try {
                player = Optional.ofNullable(players.get(UUID.fromString(val)));
            } catch (Exception e) {
                // Ignore
            }
        }

        if (!player.isPresent()) {
            try {
                String[] addressPort = val.split(":");
                InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(addressPort[0]), Integer.parseInt(addressPort[1]));
                player = Optional.ofNullable(players.get(address));
            } catch (Exception e) {
                // Ignore
            }
        }
        return player;
    }

    public Optional<AdvancedBanPlayer> getPlayer(InetSocketAddress address) {
        return Optional.ofNullable(players.get(address));
    }

    public Collection<AdvancedBanPlayer> getOnlinePlayers() {
        return Collections.unmodifiableList(new ArrayList<>(players.values()));
    }

    public Optional<InetAddress> getAddress(Object value) {
        if (value instanceof InetAddress) return Optional.of((InetAddress) value);
        return Optional.ofNullable(addresses.get(value));
    }

    protected abstract void onRegisterCommand(AbstractCommand command);

    protected abstract void log(Level level, String msg);

    public abstract String getVersion();

    public abstract void executeCommand(String command);

    public abstract Path getDataFolderPath();

    public abstract void scheduleRepeatingAsyncTask(Runnable runnable, long delay, long period);

    public abstract void scheduleAsyncTask(Runnable runnable, long delay);

    public abstract void runAsyncTask(Runnable runnable);

    public abstract void runSyncTask(Runnable runnable);

    public abstract boolean isOnlineMode();

    public abstract void callPunishmentEvent(Punishment punishment);

    public abstract void callRevokePunishmentEvent(Punishment punishment, boolean massClear);

    public abstract Optional<UUID> getInternalUUID(String name);

    public abstract boolean isUnitTesting();
}
