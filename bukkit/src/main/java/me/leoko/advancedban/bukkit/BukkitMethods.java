package me.leoko.advancedban.bukkit;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.bukkit.event.PunishmentEvent;
import me.leoko.advancedban.bukkit.event.RevokePunishmentEvent;
import me.leoko.advancedban.bukkit.listener.CommandReceiver;
import me.leoko.advancedban.manager.DatabaseManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.utils.Permissionable;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.tabcompletion.TabCompleter;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * Created by Leoko @ dev.skamps.eu on 23.07.2016.
 */
public class BukkitMethods implements MethodInterface {

    private static final int BSTATS_PLUGIN_ID = 1225;

    private final File messageFile = new File(getDataFolder(), "Messages.yml");
    private final File layoutFile = new File(getDataFolder(), "Layouts.yml");
    private final File mysqlFile = new File(getDataFolder(), "MySQL.yml");
    private YamlConfiguration config;
    private File configFile = new File(getDataFolder(), "config.yml");
    private YamlConfiguration messages;
    private YamlConfiguration layouts;
    private YamlConfiguration mysql;
    private BiFunction<OfflinePlayer, String, Boolean> permissionVault;

    public BukkitMethods() {
        // Vault support
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
            // Looked up on demand as the permission plugin might register its service after we are enabled
            permissionVault = (player, perms) -> {
                RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
                return rsp != null && rsp.getProvider().playerHas(null, player, perms);
            };
        }
    }

    @Override
    public void loadFiles() {
        if (!configFile.exists()) {
            getPlugin().saveResource("config.yml", true);
        }
        if (!messageFile.exists()) {
            getPlugin().saveResource("Messages.yml", true);
        }
        if (!layoutFile.exists()) {
            getPlugin().saveResource("Layouts.yml", true);
        }

        try {
            config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));
            messages = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(messageFile), StandardCharsets.UTF_8));
            layouts = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(layoutFile), StandardCharsets.UTF_8));

            if (mysqlFile.exists()) {
                mysql = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(mysqlFile), StandardCharsets.UTF_8));
            } else {
                mysql = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));
            }
        } catch (FileNotFoundException exc) {
            // We just saved the files, so that should really not happen.
            Universal.get().debugException(exc);
        }
    }

    @Override
    public String getVersion() {
        return getPlugin().getDescription().getVersion();
    }

    @Override
    public String[] getKeys(Object file, String path) {
        ConfigurationSection section = ((YamlConfiguration) file).getConfigurationSection(path);
        return section == null ? new String[0] : section.getKeys(false).toArray(new String[0]);
    }

    @Override
    public YamlConfiguration getConfig() {
        return config;
    }

    @Override
    public YamlConfiguration getMessages() {
        return messages;
    }

    @Override
    public YamlConfiguration getLayouts() {
        return layouts;
    }

    @Override
    public void setupMetrics() {
        Metrics metrics = new Metrics(getPlugin(), BSTATS_PLUGIN_ID);
        metrics.addCustomChart(new SimplePie("MySQL", () -> DatabaseManager.get().isUseMySQL() ? "yes" : "no"));
    }

    @Override
    public boolean isBungee() {
        return false;
    }

    @Override
    public String clearFormatting(String text) {
        return ChatColor.stripColor(text);
    }

    @Override
    public JavaPlugin getPlugin() {
        return BukkitMain.get();
    }

    @Override
    public File getDataFolder() {
        return getPlugin().getDataFolder();
    }

    @Override
    public void setCommandExecutor(String cmd, String permission, TabCompleter tabCompleter) {
        boolean friendly = getBoolean(getConfig(), "Friendly Register Commands", false);
        PluginCommand command = (friendly) ? getPlugin().getCommand(cmd) : Bukkit.getPluginCommand(cmd);
        if (command != null) {
            command.setExecutor(CommandReceiver.get());
            if (tabCompleter != null)
                command.setTabCompleter((commandSender, c, s, args) -> {
                    if (permission != null && !Universal.get().hasPerms(commandSender, permission))
                        return Collections.emptyList();
                    return tabCompleter.onTabComplete(commandSender, args);
                });
        } else {
            Universal.get().log("Failed to register command " + cmd);
        }
    }

    @Override
    public void sendMessage(Object player, String msg) {
        ((CommandSender) player).sendMessage(msg);
    }

    @Override
    public boolean hasPerms(Object player, String perms) {
        return ((CommandSender) player).hasPermission(perms);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Permissionable getOfflinePermissionPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if (permissionVault == null || player == null || !player.hasPlayedBefore())
            return permission -> false;

        return permission -> permissionVault.apply(player, permission);
    }

    @Override
    public boolean isOnline(String name) {
        return getPlayer(name) != null;
    }

    @Override
    public Player getPlayer(String name) {
        // Bukkit.getPlayer would also return players whose name just starts with the given name
        return Bukkit.getPlayerExact(name);
    }

    @Override
    public void kickPlayer(String player, String reason) {
        Player target = getPlayer(player);
        if (target != null) {
            target.kickPlayer(reason);
        }
    }

    @Override
    public Player[] getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().toArray(new Player[]{});
    }

    @Override
    public void scheduleAsyncRep(Runnable rn, long l1, long l2) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(getPlugin(), rn, l1, l2);
    }

    @Override
    public void scheduleAsync(Runnable rn, long l1) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(getPlugin(), rn, l1);
    }

    @Override
    public void runAsync(Runnable rn) {
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), rn);
    }

    @Override
    public void runSync(Runnable rn) {
        Bukkit.getScheduler().runTask(getPlugin(), rn);
    }

    @Override
    public void executeCommand(String cmd) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }

    @Override
    public String getName(Object player) {
        return ((CommandSender) player).getName();
    }

    @Override
    public String getName(String uuid) {
        UUID parsedUuid = UUIDManager.get().fromString(uuid);
        return parsedUuid == null ? null : Bukkit.getOfflinePlayer(parsedUuid).getName();
    }

    @Override
    public String getIP(Object player) {
        InetSocketAddress address = ((Player) player).getAddress();
        return address == null ? null : address.getAddress().getHostAddress();
    }

    @Override
    public String getInternUUID(Object player) {
        return player instanceof OfflinePlayer ? ((OfflinePlayer) player).getUniqueId().toString().replaceAll("-", "") : "none";
    }

    @SuppressWarnings("deprecation")
    @Override
    public String getInternUUID(String player) {
        return Bukkit.getOfflinePlayer(player).getUniqueId().toString().replaceAll("-", "");
    }

    @Override
    public boolean callChat(Object player) {
        Punishment pnt = PunishmentManager.get().getMute(UUIDManager.get().getUUID(getName(player)));
        if (pnt != null) {
            pnt.getLayout().forEach(str -> sendMessage(player, str));
            return true;
        }
        return false;
    }

    @Override
    public boolean callCMD(Object player, String cmd) {
        Punishment pnt;
        if (Universal.get().isMuteCommand(cmd.substring(1)) && (pnt = PunishmentManager.get().getMute(UUIDManager.get().getUUID(getName(player)))) != null) {
            pnt.getLayout().forEach(str -> sendMessage(player, str));
            return true;
        }
        return false;
    }

    @Override
    public YamlConfiguration getMySQLFile() {
        return mysql;
    }

    @Override
    public Boolean getBoolean(Object file, String path) {
        return ((YamlConfiguration) file).getBoolean(path);
    }

    @Override
    public String getString(Object file, String path) {
        return ((YamlConfiguration) file).getString(path);
    }

    @Override
    public Long getLong(Object file, String path) {
        return ((YamlConfiguration) file).getLong(path);
    }

    @Override
    public Integer getInteger(Object file, String path) {
        return ((YamlConfiguration) file).getInt(path);
    }

    @Override
    public List<String> getStringList(Object file, String path) {
        return ((YamlConfiguration) file).getStringList(path);
    }

    @Override
    public boolean getBoolean(Object file, String path, boolean def) {
        return ((YamlConfiguration) file).getBoolean(path, def);
    }

    @Override
    public String getString(Object file, String path, String def) {
        return ((YamlConfiguration) file).getString(path, def);
    }

    @Override
    public long getLong(Object file, String path, long def) {
        return ((YamlConfiguration) file).getLong(path, def);
    }

    @Override
    public int getInteger(Object file, String path, int def) {
        return ((YamlConfiguration) file).getInt(path, def);
    }

    @Override
    public boolean contains(Object file, String path) {
        return ((YamlConfiguration) file).contains(path);
    }

    @Override
    public String getFileName(Object file) {
        return ((YamlConfiguration) file).getName();
    }

    @Override
    public void callPunishmentEvent(Punishment punishment) {
        runSync(() -> Bukkit.getPluginManager().callEvent(new PunishmentEvent(punishment)));
    }

    @Override
    public void callRevokePunishmentEvent(Punishment punishment, boolean massClear) {
        runSync(() -> Bukkit.getPluginManager().callEvent(new RevokePunishmentEvent(punishment, massClear)));
    }

    @Override
    public boolean isOnlineMode() {
        return Bukkit.getOnlineMode();
    }

    @Override
    public void notify(String perm, List<String> notification) {
        Bukkit.getOnlinePlayers()
                .stream()
                .filter(player -> hasPerms(player, perm))
                .forEach(player -> notification.forEach(str -> sendMessage(player, str)));
    }

    @Override
    public void log(String msg) {
        Bukkit.getServer().getConsoleSender().sendMessage(msg.replaceAll("&", "§"));
    }

    @Override
    public boolean isUnitTesting() {
        return false;
    }
}