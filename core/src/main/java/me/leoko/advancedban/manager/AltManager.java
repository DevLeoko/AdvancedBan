package me.leoko.advancedban.manager;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.SQLQuery;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Remembers which accounts have been using which IP in order to find alternative accounts and ban evasion.
 */
public class AltManager {

    private static final String AUTO_BAN_OPERATOR = "AltDetection";
    private static final String AUTO_BAN_CALCULATION = "altdetection";
    private static final long PURGE_INTERVAL = TimeUnit.HOURS.toMillis(1);

    private static AltManager instance = null;
    private volatile long lastPurge = 0;

    /**
     * Get the alt manager.
     *
     * @return the alt manager instance
     */
    public static synchronized AltManager get() {
        return instance == null ? instance = new AltManager() : instance;
    }

    private static MethodInterface mi() {
        return Universal.get().getMethods();
    }

    /**
     * Check whether logins get recorded and evaluated.
     *
     * @return whether alt detection is enabled
     */
    public boolean isEnabled() {
        return mi().getBoolean(mi().getConfig(), "AltDetection.Enabled", true);
    }

    /**
     * Forgets the logins which are older than the configured retention.
     */
    public void setup() {
        lastPurge = TimeManager.getTime();
        int days = mi().getInteger(mi().getConfig(), "AltDetection.Retention Days", 90);
        if (days > 0) {
            DatabaseManager.get().executeStatement(SQLQuery.DELETE_OLD_LOGINS, TimeManager.getTime() - TimeUnit.DAYS.toMillis(days));
        }
    }

    /**
     * Remember that an account has been using an IP. A failure in here never affects the connection.
     *
     * @param name the accounts name
     * @param uuid the accounts uuid
     * @param ip   the ip
     */
    public void recordLogin(String name, String uuid, String ip) {
        if (!isEnabled() || ip == null) {
            return;
        }

        try {
            // A server can be running for much longer than the retention
            if (TimeManager.getTime() - lastPurge > PURGE_INTERVAL) {
                setup();
            }

            String address = normalize(ip);
            if (!isIgnored(address)) {
                DatabaseManager.get().executeStatement(SQLQuery.DELETE_LOGIN, uuid, address);
                DatabaseManager.get().executeStatement(SQLQuery.INSERT_LOGIN, uuid, name, address, TimeManager.getTime());
            }
        } catch (RuntimeException ex) {
            // Nobody should be kept from joining because of this
            Universal.get().log("An error has occurred recording the login of " + name);
            Universal.get().debugException(ex);
        }
    }

    /**
     * Get the other accounts which have been using an IP of the given account.
     * IPs used by more accounts than the configured limit say nothing about who is behind them and are left out.
     *
     * @param uuid the accounts uuid
     * @return the other accounts, most recently seen first
     */
    public List<Account> getAlts(String uuid) {
        List<Login> logins = query(SQLQuery.SELECT_LOGIN_ALTS, uuid, uuid);
        int limit = mi().getInteger(mi().getConfig(), "AltDetection.Shared IP Limit", 10);

        Map<String, Set<String>> accountsByIp = new HashMap<>();
        for (Login login : logins) {
            accountsByIp.computeIfAbsent(login.ip, ip -> new HashSet<>()).add(login.account.getUuid());
        }

        // The given account is not part of the result, but it does count as a user of the IP
        return newestPerAccount(logins.stream()
                .filter(login -> accountsByIp.get(login.ip).size() + 1 <= limit && !isIgnored(login.ip))
                .collect(Collectors.toList()));
    }

    /**
     * Get the accounts which have been using an IP.
     *
     * @param ip the ip
     * @return the accounts, most recently seen first
     */
    public List<Account> getAccounts(String ip) {
        return newestPerAccount(query(SQLQuery.SELECT_LOGINS_BY_IP, normalize(ip)));
    }

    /**
     * Looks for a banned account behind the IPs of a connecting player, alerts the staff and if configured bans the
     * player as well. A failure in here never affects the connection.
     *
     * @param name the connecting players name
     * @param uuid the connecting players uuid
     * @param ip   the ip the player is connecting from
     * @return the message to deny the connection with or <code>null</code> if the player may join
     */
    public String checkEvasion(String name, String uuid, String ip) {
        if (!isEnabled() || Universal.get().isExemptPlayer(name)) {
            return null;
        }

        try {
            Set<String> onCurrentIp = ip == null ? new HashSet<>() : getAccounts(ip).stream().map(Account::getUuid).collect(Collectors.toSet());

            Account evadedAccount = null;
            Punishment evadedBan = null;
            for (Account alt : getAlts(uuid)) {
                Punishment ban = PunishmentManager.get().getBan(alt.getUuid());
                // Accounts banned by this feature say nothing, otherwise it would spread from one connection to the next
                if (ban == null || AUTO_BAN_OPERATOR.equals(ban.getOperator())) {
                    continue;
                }
                if (onCurrentIp.contains(alt.getUuid())) {
                    return handleEvasion(name, uuid, alt, ban, true);
                }
                if (evadedAccount == null) {
                    evadedAccount = alt;
                    evadedBan = ban;
                }
            }
            return evadedAccount == null ? null : handleEvasion(name, uuid, evadedAccount, evadedBan, false);
        } catch (RuntimeException ex) {
            Universal.get().log("An error has occurred looking for alternative accounts of " + name);
            Universal.get().debugException(ex);
            return null;
        }
    }

    private String handleEvasion(String name, String uuid, Account alt, Punishment ban, boolean currentIp) {
        MethodInterface mi = mi();
        Universal.get().log(name + " shares an IP with the banned account " + alt.getName());
        mi.notify("ab.alts.notify", MessageManager.getLayout(mi.getMessages(), "AltDetection.Notification",
                "PREFIX", mi.getBoolean(mi.getConfig(), "Disable Prefix", false) ? "" : MessageManager.getMessage("General.Prefix"),
                "NAME", name,
                "ALT", alt.getName(),
                "REASON", ban.getReason()));

        if (!mi.getBoolean(mi.getConfig(), "AltDetection.AutoBan.Enabled", false)
                // Only the IP in use right now is good enough, the other one could have been a friend's or reassigned by now
                || !currentIp
                // Anyone could connect using the name of someone else, and that one would get banned
                || !mi.isOnlineMode()
                // There is one chance per account: otherwise it would be impossible to unban it
                || PunishmentManager.get().getCalculationLevel(uuid, AUTO_BAN_CALCULATION) > 0
                || mi.getOfflinePermissionPlayer(name).hasPermission("ab.ban.exempt")) {
            return null;
        }

        String reason = mi.getString(mi.getConfig(), "AltDetection.AutoBan.Reason", "Ban evasion (alt of %ALT%)")
                .replace("%ALT%", alt.getName());
        // It lasts as long as the ban which got evaded
        Punishment evasionBan = new Punishment(name, uuid, reason, AUTO_BAN_OPERATOR, ban.getType(),
                TimeManager.getTime(), ban.getEnd(), AUTO_BAN_CALCULATION, -1);
        evasionBan.create();
        return evasionBan.getLayoutBSN();
    }

    private boolean isIgnored(String ip) {
        return mi().getStringList(mi().getConfig(), "AltDetection.IgnoredIPs").stream()
                .anyMatch(ignored -> normalize(ignored).equals(ip));
    }

    // IPv6 addresses have many spellings, and connections might add a scope like "%eth0"
    private static String normalize(String ip) {
        if (!ip.contains(":")) {
            return ip;
        }
        String address = ip.contains("%") ? ip.substring(0, ip.indexOf('%')) : ip;
        // Anything else would be looked up in the DNS, and this can be whatever someone typed into /alts
        if (!address.matches("[0-9a-fA-F:][0-9a-fA-F:.]*")) {
            return address;
        }
        try {
            return InetAddress.getByName(address).getHostAddress();
        } catch (UnknownHostException ex) {
            return address;
        }
    }

    private List<Login> query(SQLQuery query, Object... parameters) {
        List<Login> logins = new ArrayList<>();
        try (ResultSet rs = DatabaseManager.get().executeResultStatement(query, parameters)) {
            while (rs != null && rs.next()) {
                logins.add(new Login(rs.getString("ip"),
                        new Account(rs.getString("uuid"), rs.getString("name"), rs.getLong("lastSeen"))));
            }
        } catch (SQLException ex) {
            Universal.get().log("An error has occurred reading the login history.");
            Universal.get().debugSqlException(ex);
        }
        return logins;
    }

    private static List<Account> newestPerAccount(Collection<Login> logins) {
        Map<String, Account> accounts = new LinkedHashMap<>();
        for (Login login : logins) {
            accounts.merge(login.account.getUuid(), login.account,
                    (a, b) -> a.getLastSeen() >= b.getLastSeen() ? a : b);
        }
        List<Account> result = new ArrayList<>(accounts.values());
        result.sort(Comparator.comparingLong(Account::getLastSeen).reversed());
        return result;
    }

    private static class Login {
        private final String ip;
        private final Account account;

        private Login(String ip, Account account) {
            this.ip = ip;
            this.account = account;
        }
    }

    /**
     * An account found in the login history.
     */
    public static class Account {
        private final String uuid;
        private final String name;
        private final long lastSeen;

        private Account(String uuid, String name, long lastSeen) {
            this.uuid = uuid;
            this.name = name;
            this.lastSeen = lastSeen;
        }

        public String getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }

        public long getLastSeen() {
            return lastSeen;
        }
    }
}
