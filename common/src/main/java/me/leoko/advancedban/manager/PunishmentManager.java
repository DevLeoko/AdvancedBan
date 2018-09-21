package me.leoko.advancedban.manager;

import lombok.RequiredArgsConstructor;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanPlayer;
import me.leoko.advancedban.punishment.InterimData;
import me.leoko.advancedban.punishment.Punishment;
import me.leoko.advancedban.punishment.PunishmentType;
import me.leoko.advancedban.utils.SQLQuery;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by Leoko @ dev.skamps.eu on 30.05.2016.
 */
@RequiredArgsConstructor
public class PunishmentManager {
    private final Set<Punishment> punishments = Collections.synchronizedSet(new HashSet<>());
    private final Set<Punishment> history = Collections.synchronizedSet(new HashSet<>());
    private final Set<Object> cached = Collections.synchronizedSet(new HashSet<>());
    private final AdvancedBan advancedBan;

    public void onEnable() {
        advancedBan.getDatabaseManager().executeStatement(SQLQuery.DELETE_OLD_PUNISHMENTS, advancedBan.getTimeManager().getTime());

        advancedBan.getOnlinePlayers().forEach(player -> load(player.getUniqueId(), player.getName(), player.getAddress().getAddress()));
    }

    public InterimData load(UUID uuid, String name, InetAddress address) {
        Set<Punishment> punishments = new HashSet<>();
        Set<Punishment> history = new HashSet<>();
        try {
            ResultSet rs = advancedBan.getDatabaseManager().executeResultStatement(SQLQuery.SELECT_USER_PUNISHMENTS_WITH_IP, uuid, address.getHostAddress());
            while (rs.next()) {
                punishments.add(getPunishmentFromResultSet(rs));
            }
            rs.close();

            rs = advancedBan.getDatabaseManager().executeResultStatement(SQLQuery.SELECT_USER_PUNISHMENTS_HISTORY_WITH_IP, uuid, address.getHostAddress());
            while (rs.next()) {
                history.add(getPunishmentFromResultSet(rs));
            }
            rs.close();
        } catch (SQLException ex) {
            advancedBan.getLogger().warn("An error has ocurred loading the punishments from the database.");
            advancedBan.getLogger().logException(ex);
        }
        return new InterimData(uuid, name, address, punishments, history);
    }

    public void discard(AdvancedBanPlayer player) {
        cached.remove(player.getName());
        cached.remove(player.getUniqueId());
        cached.remove(player.getAddress());

        Predicate<Punishment> remove = pun -> pun.getIdentifier().equals(player.getUniqueId()) ||
                pun.getIdentifier().equals(player.getAddress().getAddress());

        punishments.removeIf(remove);
        history.removeIf(remove);
    }

    public List<Punishment> getPunishments(Object identifier, PunishmentType type, boolean current) {
        List<Punishment> punishments = new ArrayList<>();

        if (isCached(identifier)) {
            for (Iterator<Punishment> iterator = (current ? this.punishments : history).iterator(); iterator.hasNext(); ) {
                Punishment punishment = iterator.next();
                if ((type == null || type == punishment.getType().getBasic()) && punishment.getIdentifier().equals(identifier)) {
                    if (!current || !punishment.isExpired()) {
                        punishments.add(punishment);
                    } else {
                        punishment.delete(null, false, false);
                        iterator.remove();
                    }
                }
            }
        } else {
            try (ResultSet rs = advancedBan.getDatabaseManager().
                    executeResultStatement(current ? SQLQuery.SELECT_USER_PUNISHMENTS : SQLQuery.SELECT_USER_PUNISHMENTS_HISTORY,
                            identifier.toString())) {
                while (rs.next()) {
                    Punishment punishment = getPunishmentFromResultSet(rs);
                    if ((type == null || type == punishment.getType().getBasic()) && (!current || !punishment.isExpired())) {
                        punishments.add(punishment);
                    }
                }
            } catch (SQLException ex) {
                advancedBan.getLogger().info("An error has occurred getting the punishments for " + identifier);
                advancedBan.getLogger().logException(ex);
            }
        }
        return punishments;
    }

    public List<Punishment> getPunishments(SQLQuery sqlQuery, Object... parameters) {
        List<Punishment> ptList = new ArrayList<>();

        ResultSet rs = advancedBan.getDatabaseManager().executeResultStatement(sqlQuery, parameters);
        try {
            while (rs.next()) {
                Punishment punishment = getPunishmentFromResultSet(rs);
                ptList.add(punishment);
            }
            rs.close();
        } catch (SQLException ex) {
            advancedBan.getLogger().info("An error has occurred executing a query in the database.");
            advancedBan.getLogger().debug("Query: \n" + sqlQuery);
            advancedBan.getLogger().logException(ex);
        }
        return ptList;
    }

    public Optional<Punishment> getPunishment(int id) {
        ResultSet rs = advancedBan.getDatabaseManager().executeResultStatement(SQLQuery.SELECT_PUNISHMENT_BY_ID, id);
        Punishment pt = null;
        try {
            if (rs.next()) {
                pt = getPunishmentFromResultSet(rs);
            }
            rs.close();
        } catch (SQLException ex) {
            advancedBan.getLogger().info("An error has ocurred getting a punishment by his id.");
            advancedBan.getLogger().debug("Punishment id: '" + id + "'");
            advancedBan.getLogger().logException(ex);
        }
        return pt == null || pt.isExpired() ? Optional.empty() : Optional.of(pt);
    }

    public Optional<Punishment> getWarn(int id) {
        Optional<Punishment> punishment = getPunishment(id);
        return punishment.isPresent() && punishment.get().getType().getBasic() == PunishmentType.WARNING ? punishment : Optional.empty();
    }

    public List<Punishment> getWarns(Object object) {
        return getPunishments(object, PunishmentType.WARNING, true);
    }

    public Optional<Punishment> getBan(Object object) {
        List<Punishment> punishments = getPunishments(object, PunishmentType.BAN, true);
        return punishments.isEmpty() ? Optional.empty() : Optional.ofNullable(punishments.get(0));
    }

    public Optional<Punishment> getMute(Object object) {
        List<Punishment> punishments = getPunishments(object, PunishmentType.MUTE, true);
        return punishments.isEmpty() ? Optional.empty() : Optional.ofNullable(punishments.get(0));
    }

    public boolean isBanned(Object object) {
        return getBan(object).isPresent();
    }

    public boolean isMuted(Object object) {
        return getMute(object).isPresent();
    }

    public boolean isCached(Object name) {
        return cached.contains(name);
    }

    public void addCached(Object object) {
        cached.add(object);
    }

    public int getCalculationLevel(Object identifier, String layout) {
        if (isCached(identifier)) {
            return (int) history.stream().filter(pt -> pt.getIdentifier().equals(identifier) && layout.equalsIgnoreCase(pt.getCalculation())).count();
        } else {
            ResultSet resultSet = advancedBan.getDatabaseManager().executeResultStatement(SQLQuery.SELECT_USER_PUNISHMENTS_HISTORY_BY_CALCULATION, identifier.toString(), layout);
            int i = 0;
            try {
                while (resultSet.next()) {
                    i++;
                }
                resultSet.close();
            } catch (SQLException ex) {
                advancedBan.getLogger().warn("An error has occurred getting the level for the layout '" + layout + "' for '" + identifier + "'");
                advancedBan.getLogger().logException(ex);
            }
            return i;
        }
    }

    public int getCurrentWarns(Object object) {
        return getWarns(object).size();
    }

    public Set<Punishment> getLoadedPunishments(boolean checkExpired) {
        if (checkExpired) {
            List<Punishment> toDelete = new ArrayList<>();
            for (Punishment pu : punishments) {
                if (pu.isExpired()) {
                    toDelete.add(pu);
                }
            }
            for (Punishment pu : toDelete) {
                pu.delete();
            }
        }
        return punishments;
    }

    /*
    public long getCalculation(String layout, String name, String uuid) {
        long end = TimeManager.getTime();
        MethodInterface mi = Universal.get().getMethods();

        int i = getCalculationLevel(name, uuid);

        List<String> timeLayout = mi.getStringList(mi.getLayouts(), "Time." + layout);
        String time = timeLayout.get(timeLayout.size() <= i ? timeLayout.size() - 1 : i);
        long toAdd = TimeManager.toMilliSec(time.toLowerCase());
        end += toAdd;

        return end;
    }
    */

    public Punishment getPunishmentFromResultSet(ResultSet rs) throws SQLException {
        Punishment punishment = new Punishment(
                advancedBan,
                UUID.fromString(rs.getString("uuid")),
                rs.getString("name"),
                rs.getString("operator"),
                rs.getString("calculation"),
                rs.getLong("start"),
                rs.getLong("end"),
                PunishmentType.valueOf(rs.getString("punishmentType"))
        );
        punishment.setReason(rs.getString("reason"));
        punishment.setId(rs.getInt("id"));

        return punishment;
    }

    public Set<Punishment> getLoadedHistory() {
        return history;
    }
}