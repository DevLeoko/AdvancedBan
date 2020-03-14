package me.leoko.advancedban.utils;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.DatabaseManager;
import me.leoko.advancedban.manager.MessageManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.utils.commands.ListProcessor;
import me.leoko.advancedban.utils.commands.PunishmentProcessor;
import me.leoko.advancedban.utils.commands.RevokeByIdProcessor;
import me.leoko.advancedban.utils.commands.RevokeProcessor;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static me.leoko.advancedban.utils.CommandUtils.*;

public enum Command {
    BAN(
            PunishmentType.BAN.getPerms(),
            ".+",
            new PunishmentProcessor(PunishmentType.BAN),
            PunishmentType.BAN.getConfSection("Usage"),
            "ban"),

    TEMP_BAN(
            PunishmentType.TEMP_BAN.getPerms(),
            "\\S+ [1-9][0-9]*([wdhms]|mo)( .*)?",
            new PunishmentProcessor(PunishmentType.TEMP_BAN),
            PunishmentType.TEMP_BAN.getConfSection("Usage"),
            "tempban"),

    IP_BAN(
            PunishmentType.IP_BAN.getPerms(),
            ".+",
            new PunishmentProcessor(PunishmentType.IP_BAN),
            PunishmentType.IP_BAN.getConfSection("Usage"),
            "ipban", "banip", "ban-ip"),

    MUTE(
            PunishmentType.MUTE.getPerms(),
            ".+",
            new PunishmentProcessor(PunishmentType.MUTE),
            PunishmentType.MUTE.getConfSection("Usage"),
            "mute"),

    TEMP_MUTE(
            PunishmentType.TEMP_MUTE.getPerms(),
            "\\S+ [1-9][0-9]*([wdhms]|mo)( .*)?",
            new PunishmentProcessor(PunishmentType.TEMP_MUTE),
            PunishmentType.TEMP_MUTE.getConfSection("Usage"),
            "tempmute"),

    WARN(
            PunishmentType.WARNING.getPerms(),
            ".+",
            new PunishmentProcessor(PunishmentType.WARNING),
            PunishmentType.WARNING.getConfSection("Usage"),
            "warn"),

    TEMP_WARN(
            PunishmentType.TEMP_WARNING.getPerms(),
            "\\S+ [1-9][0-9]*([wdhms]|mo)( .*)?",
            new PunishmentProcessor(PunishmentType.TEMP_WARNING),
            PunishmentType.TEMP_WARNING.getConfSection("Usage"),
            "tempwarn"),

    KICK(
            PunishmentType.KICK.getPerms(),
            ".+",
            input -> {
                if (!Universal.get().getMethods().isOnline(input.getPrimaryData())) {
                    MessageManager.sendMessage(input.getSender(), "Kick.NotOnline", true,
                            "NAME", input.getPrimary());
                    return;
                }

                new PunishmentProcessor(PunishmentType.KICK).accept(input);
            },
            PunishmentType.KICK.getConfSection("Usage"),
            "kick"),

    UN_BAN("ab." + PunishmentType.BAN.getName() + ".undo",
            "\\S+",
            new RevokeProcessor(PunishmentType.BAN),
            "Un" + PunishmentType.BAN.getConfSection("Usage"),
            "unban"),

    UN_MUTE("ab." + PunishmentType.MUTE.getName() + ".undo",
            "\\S+",
            new RevokeProcessor(PunishmentType.MUTE),
            "Un" + PunishmentType.MUTE.getConfSection("Usage"),
            "unmute"),

    UN_WARN("ab." + PunishmentType.WARNING.getName() + ".undo",
            "[0-9]+|(?i:clear \\S+)",
            input -> {
                final String confSection = PunishmentType.WARNING.getConfSection();
                if (input.getPrimaryData().equals("clear")) {
                    input.next();
                    String name = input.getPrimary();
                    String uuid = processName(input);
                    if (uuid == null)
                        return;

                    List<Punishment> punishments = PunishmentManager.get().getWarns(uuid);
                    if (!punishments.isEmpty()) {
                        MessageManager.sendMessage(input.getSender(), "Un" + confSection + ".Clear.Empty",
                                true, "NAME", name);
                        return;
                    }

                    String operator = Universal.get().getMethods().getName(input.getSender());
                    for (Punishment punishment : punishments) {
                        punishment.delete(operator, true, true);
                    }
                    MessageManager.sendMessage(input.getSender(), "Un" + confSection + ".Clear.Done",
                            true, "COUNT", String.valueOf(punishments.size()));
                } else {
                    new RevokeByIdProcessor("Un" + confSection, PunishmentManager.get()::getWarn).accept(input);
                }
            },
            "Un" + PunishmentType.WARNING.getConfSection("Usage"),
            "unwarn"),

    UN_PUNISH("ab.all.undo",
            "[0-9]+",
            new RevokeByIdProcessor("UnPunish", PunishmentManager.get()::getWarn),
            "UnPunish.Usage",
            "unpunish"),

    CHANGE_REASON("ab.changeReason",
            "([0-9]+|(ban|mute) \\S+) .+",
            input -> {
                Punishment punishment;

                if (input.getPrimaryData().matches("[0-9]*")) {
                    input.next();
                    int id = Integer.parseInt(input.getPrimaryData());

                    punishment = PunishmentManager.get().getPunishment(id);
                } else {
                    PunishmentType type = PunishmentType.valueOf(input.getPrimary());
                    input.next();

                    String target = input.getPrimary();
                    if (!target.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
                        target = processName(input);
                        if (target == null)
                            return;
                    } else {
                        input.next();
                    }

                    punishment = getPunishment(target, type);
                }

                String reason = processReason(input);
                if (reason == null)
                    return;

                if (punishment != null) {
                    punishment.updateReason(reason);
                    MessageManager.sendMessage(input.getSender(), "ChangeReason.Done",
                            true, "ID", String.valueOf(punishment.getId()));
                } else {
                    MessageManager.sendMessage(input.getSender(), "ChangeReason.NotFound", true);
                }
            },
            "ChangeReason.Usage",
            "change-reason"),

    BAN_LIST("ab.banlist",
            "([1-9][0-9]*)?",
            new ListProcessor(
                    target -> PunishmentManager.get().getPunishments(SQLQuery.SELECT_ALL_PUNISHMENTS_LIMIT, 150),
                    "Banlist", false, false),
            "Banlist.Usage",
            "banlist"),

    HISTORY("ab.history",
            "\\S+( [1-9][0-9]*)?",
            new ListProcessor(
                    target -> PunishmentManager.get().getPunishments(target, null, false),
                    "History", true, true),
            "Banlist.Usage",
            "history"),

    WARNS(null,
            "\\S+( [1-9][0-9]*)?|\\S+",
            input -> {
                if (input.getPrimary().matches("\\S+")) {
                    if (!Universal.get().hasPerms(input.getSender(), "ab.warns.other")) {
                        MessageManager.sendMessage(input.getSender(), "General.NoPerms", true);
                        return;
                    }

                    new ListProcessor(
                            target -> PunishmentManager.get().getPunishments(target, null, true),
                            "Warns", false, true).accept(input);
                } else {
                    if (!Universal.get().hasPerms(input.getSender(), "ab.warns.own")) {
                        MessageManager.sendMessage(input.getSender(), "General.NoPerms", true);
                        return;
                    }

                    String name = Universal.get().getMethods().getName(input.getSender());
                    new ListProcessor(
                            target -> PunishmentManager.get().getPunishments(name, null, true),
                            "Warns", false, false).accept(input);
                }
            },
            "Warns.Usage",
            "warns"),

    CHECK("ab.check",
            "\\S+",
            input -> {
                String name = input.getPrimary();

                String uuid = processName(input);
                if (uuid == null)
                    return;


                String ip = Universal.get().getIps().getOrDefault(name.toLowerCase(), "none cashed");
                String loc = Universal.get().getMethods().getFromUrlJson("http://ip-api.com/json/" + ip, "country");
                Punishment mute = PunishmentManager.get().getMute(uuid);
                Punishment ban = PunishmentManager.get().getBan(uuid);

                Object sender = input.getSender();
                MessageManager.sendMessage(sender, "Check.Header", true, "NAME", name);
                MessageManager.sendMessage(sender, "Check.UUID", false, "UUID", uuid);
                if (Universal.get().hasPerms(sender, "ab.check.ip")) {
                    MessageManager.sendMessage(sender, "Check.IP", false, "IP", ip);
                }
                MessageManager.sendMessage(sender, "Check.Geo", false, "LOCATION", loc == null ? "failed!" : loc);
                MessageManager.sendMessage(sender, "Check.Mute", false, "DURATION", mute == null ? "§anone" : mute.getType().isTemp() ? "§e" + mute.getDuration(false) : "§cperma");
                if (mute != null) {
                    MessageManager.sendMessage(sender, "Check.MuteReason", false, "REASON", mute.getReason());
                }
                MessageManager.sendMessage(sender, "Check.Ban", false, "DURATION", ban == null ? "§anone" : ban.getType().isTemp() ? "§e" + ban.getDuration(false) : "§cperma");
                if (ban != null) {
                    MessageManager.sendMessage(sender, "Check.BanReason", false, "REASON", ban.getReason());
                }
                MessageManager.sendMessage(sender, "Check.Warn", false, "COUNT", PunishmentManager.get().getCurrentWarns(uuid) + "");
            },
            "Check.Usage",
            "check"),

    SYSTEM_PREFERENCES("ab.systemprefs",
            ".*",
            input -> {
                MethodInterface mi = Universal.get().getMethods();
                Calendar calendar = new GregorianCalendar();
                Object sender = input.getSender();
                mi.sendMessage(sender, "§c§lAdvancedBan v2 §cSystemPrefs");
                mi.sendMessage(sender, "§cServer-Time §8» §7" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
                mi.sendMessage(sender, "§cYour UUID (Intern) §8» §7" + mi.getInternUUID(sender));
                if (input.hasNext()) {
                    String target = input.getPrimaryData();
                    mi.sendMessage(sender, "§c" + target + "'s UUID (Intern) §8» §7" + mi.getInternUUID(target));
                    mi.sendMessage(sender, "§c" + target + "'s UUID (Fetched) §8» §7" + UUIDManager.get().getUUID(target));
                }
            },
            null,
            "systemPrefs"),

    ADVANCED_BAN(null,
            ".*",
            input -> {
                MethodInterface mi = Universal.get().getMethods();
                Object sender = input.getSender();
                if (input.hasNext()) {
                    if (input.getPrimaryData().equals("reload")) {
                        if (Universal.get().hasPerms(sender, "ab.reload")) {
                            mi.loadFiles();
                            mi.sendMessage(sender, "§a§lAdvancedBan §8§l» §7Reloaded!");
                        } else {
                            MessageManager.sendMessage(sender, "General.NoPerms", true);
                        }
                        return;
                    } else if (input.getPrimaryData().equals("help")) {
                        if (Universal.get().hasPerms(sender, "ab.help")) {
                            mi.sendMessage(sender, "§8");
                            mi.sendMessage(sender, "§c§lAdvancedBan §7Command-Help");
                            mi.sendMessage(sender, "§8");
                            mi.sendMessage(sender, "§c/ban [Name] [Reason/@Layout]");
                            mi.sendMessage(sender, "§8» §7Ban a user permanently");
                            mi.sendMessage(sender, "§c/banip [Name/IP] [Reason/@Layout]");
                            mi.sendMessage(sender, "§8» §7Ban a user by IP");
                            mi.sendMessage(sender, "§c/tempban [Name] [Xmo/Xd/Xh/Xm/Xs/#TimeLayout] [Reason/@Layout]");
                            mi.sendMessage(sender, "§8» §7Ban a user temporary");
                            mi.sendMessage(sender, "§c/mute [Name] [Reason/@Layout]");
                            mi.sendMessage(sender, "§8» §7Mute a user permanently");
                            mi.sendMessage(sender, "§c/tempmute [Name] [Xmo/Xd/Xh/Xm/Xs/#TimeLayout] [Reason/@Layout]");
                            mi.sendMessage(sender, "§8» §7Mute a user temporary");
                            mi.sendMessage(sender, "§c/warn [Name] [Reason/@Layout]");
                            mi.sendMessage(sender, "§8» §7Warn a user permanently");
                            mi.sendMessage(sender, "§c/tempwarn [Name] [Xmo/Xd/Xh/Xm/Xs/#TimeLayout] [Reason/@Layout]");
                            mi.sendMessage(sender, "§8» §7Warn a user temporary");
                            mi.sendMessage(sender, "§c/kick [Name] [Reason/@Layout]");
                            mi.sendMessage(sender, "§8» §7Kick a user");
                            mi.sendMessage(sender, "§c/unban [Name/IP]");
                            mi.sendMessage(sender, "§8» §7Unban a user");
                            mi.sendMessage(sender, "§c/unmute [Name]");
                            mi.sendMessage(sender, "§8» §7Unmute a user");
                            mi.sendMessage(sender, "§c/unwarn [ID] or /unwarn clear [Name]");
                            mi.sendMessage(sender, "§8» §7Deletes a warn");
                            mi.sendMessage(sender, "§c/change-reason [ID or ban/mute USER] [New reason]");
                            mi.sendMessage(sender, "§8» §7Changes the reason of a punishment");
                            mi.sendMessage(sender, "§c/unpunish [ID]");
                            mi.sendMessage(sender, "§8» §7Deletes a punishment by ID");
                            mi.sendMessage(sender, "§c/banlist <Page>");
                            mi.sendMessage(sender, "§8» §7See all punishments");
                            mi.sendMessage(sender, "§c/history [Name/IP] <Page>");
                            mi.sendMessage(sender, "§8» §7See a users history");
                            mi.sendMessage(sender, "§c/warns [Name] <Page>");
                            mi.sendMessage(sender, "§8» §7See your or a users wa");
                            mi.sendMessage(sender, "§c/check [Name]");
                            mi.sendMessage(sender, "§8» §7Get all information about a user");
                            mi.sendMessage(sender, "§c/AdvancedBan <reload/help>");
                            mi.sendMessage(sender, "§8» §7Reloads the plugin or shows help page");
                            mi.sendMessage(sender, "§8");
                        } else {
                            MessageManager.sendMessage(sender, "General.NoPerms", true);
                        }
                        return;
                    }
                }


                mi.sendMessage(sender, "§8§l§m-=====§r §c§lAdvancedBan v2 §8§l§m=====-§r ");
                mi.sendMessage(sender, "  §cDev §8• §7Leoko");
                mi.sendMessage(sender, "  §cStatus §8• §a§oStable");
                mi.sendMessage(sender, "  §cVersion §8• §7" + mi.getVersion());
                mi.sendMessage(sender, "  §cLicense §8• §7Public");
                mi.sendMessage(sender, "  §cStorage §8• §7" + (DatabaseManager.get().isUseMySQL() ? "MySQL (external)" : "HSQLDB (local)"));
                mi.sendMessage(sender, "  §cServer §8• §7" + (Universal.get().isBungee() ? "Bungeecord" : "Spigot/Bukkit"));
                if (Universal.get().isBungee()) {
                    mi.sendMessage(sender, "  §cRedisBungee §8• §7" + (Universal.get().useRedis() ? "true" : "false"));
                }
                mi.sendMessage(sender, "  §cUUID-Mode §8• §7" + UUIDManager.get().getMode());
                mi.sendMessage(sender, "  §cPrefix §8• §7" + (mi.getBoolean(mi.getConfig(), "Disable Prefix", false) ? "" : MessageManager.getMessage("General.Prefix")));
                mi.sendMessage(sender, "§8§l§m-=========================-§r ");
            },
            null,
            "advancedban");

    private final String permission;
    private final Predicate<String[]> syntaxValidator;
    private final Consumer<CommandInput> commandHandler;
    private final String usagePath;
    private final String[] names;

    Command(String permission, Predicate<String[]> syntaxValidator,
            Consumer<CommandInput> commandHandler, String usagePath, String... names) {
        this.permission = permission;
        this.syntaxValidator = syntaxValidator;
        this.commandHandler = commandHandler;
        this.usagePath = usagePath;
        this.names = names;
    }

    Command(String permission, String regex, Consumer<CommandInput> commandHandler,
            String usagePath, String... names) {
        this(permission, (args) -> String.join(" ", args).matches(regex), commandHandler, usagePath, names);
    }

    public String getPermission() {
        return permission;
    }

    public String getUsagePath() {
        return usagePath;
    }

    public boolean validateArguments(String[] args) {
        return syntaxValidator.test(args);
    }

    public void execute(Object player, String[] args) {
        commandHandler.accept(new CommandInput(player, args));
    }

    public static Command getByName(String name) {
        String lowerCase = name.toLowerCase();
        for (Command command : values()) {
            for (String s : command.names) {
                if (s.equals(lowerCase))
                    return command;
            }
        }
        return null;
    }

    public class CommandInput {
        private Object sender;
        private String[] args;

        CommandInput(Object sender, String[] args) {
            this.sender = sender;
            this.args = args;
        }

        public Object getSender() {
            return sender;
        }

        public String[] getArgs() {
            return args;
        }

        public String getPrimary() {
            return args.length == 0 ? null : args[0];
        }

        String getPrimaryData() {
            return getPrimary().toLowerCase();
        }

        public void removeArgument(int index) {
            args = ArrayUtils.remove(args, index);
        }

        public void next() {
            args = ArrayUtils.remove(args, 0);
        }

        public boolean hasNext() {
            return args.length > 0;
        }
    }
}
