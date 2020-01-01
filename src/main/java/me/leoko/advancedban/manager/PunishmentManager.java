package me.leoko.advancedban.manager;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.utils.InterimData;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;
import me.leoko.advancedban.utils.SQLQuery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * The Punishment Manager handles the punishments. It loads and parses them from the database, caches them
 * and eventually discards them again.
 */
public class PunishmentManager {

    private static PunishmentManager instance = null;
    private final Universal universal = Universal.get();
    private final Set<Punishment> punishments = Collections.synchronizedSet(new HashSet<>());
    private final Set<Punishment> history = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> cached = Collections.synchronizedSet(new HashSet<>());

    /**
     * Get the punishment manager.
     *
     * @return the punishment manager instance
     */
    public static PunishmentManager get() {
        return instance == null ? instance = new PunishmentManager() : instance;
    }

    /**
     * Initially clears out all expired punishments.
     */
    public void setup() {
        DatabaseManager.get().executeStatement(SQLQuery.DELETE_OLD_PUNISHMENTS, TimeManager.getTime());
        // Seems useless as the Interim Data which get's loaded just is ignored
//        for (Object player : mi.getOnlinePlayers()) {
//            String name = mi.getName(player).toLowerCase();
//            load(name, UUIDManager.get().getUUID(name), mi.getIP(player));
//        }
    }

    /**
     * Get a users punishments as {@link InterimData}. This method is meant to be called if the goal eventually is
     * to add the users punishments to the cache. If you are just interested in the specific punishments there are
     * more convenient methods as {@link #getBan(String)}, {@link #getMute(String)}, {@link #getWarns(String)}
     * or {@link #getPunishments(String, PunishmentType, boolean)}.
     *
     * @param name the users name
     * @param uuid the users uuid
     * @param ip   the users ip
     * @return the interim data
     */
    public InterimData load(String name, String uuid, String ip) {
	Set<Punishment> punishments = new HashSet<>();
	Set<Punishment> history = new HashSet<>();
        try (ResultSet resultsPunishments = DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_USER_PUNISHMENTS_WITH_IP, uuid, ip); ResultSet resultsHistory = DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_USER_PUNISHMENTS_HISTORY_WITH_IP, uuid, ip)) {
            
        	while (resultsPunishments.next()) {
                punishments.add(getPunishmentFromResultSet(resultsPunishments));
            } 
            while (resultsHistory.next()) {
                history.add(getPunishmentFromResultSet(resultsHistory));
            }
            
        } catch (SQLException ex) {
            universal.log("An error has occurred loading the punishments from the database.");
            universal.debug(ex);
        }
        return new InterimData(uuid, name, ip, punishments, history);
    }

    /**
     * Discard a players punishments from the cache.
     *
     * @param name the name
     */
    public void discard(String name) {
        name = name.toLowerCase();
        String ip = Universal.get().getIps().get(name);
        String uuid = UUIDManager.get().getUUID(name);
        cached.remove(name);
        cached.remove(uuid);
        cached.remove(ip);

        Iterator<Punishment> iterator = punishments.iterator();
        while (iterator.hasNext()) {
            Punishment punishment = iterator.next();
            if (punishment.getUuid().equals(uuid) || punishment.getUuid().equals(ip)) {
                iterator.remove();
            }
        }

        iterator = history.iterator();
        while (iterator.hasNext()) {
            Punishment punishment = iterator.next();
            if (punishment.getUuid().equals(uuid) || punishment.getUuid().equals(ip)) {
                iterator.remove();
            }
        }
    }

    /**
     * Get all punishments which belong to the given uuid or ip.
     *
     * @param target  the uuid or ip to search for
     * @param put     the basic punishment type to search for ({@link PunishmentType#BAN} would also include Tempbans).
     *                Use <code>null</code> to search for all punishments.
     * @param current if only active punishments should be included.
     * @return the punishments
     */
    public List<Punishment> getPunishments(String target, PunishmentType put, boolean current) {
        List<Punishment> ptList = new ArrayList<>();

        if (isCached(target)) {
            for (Iterator<Punishment> iterator = (current ? punishments : history).iterator(); iterator.hasNext();) {
                Punishment pt = iterator.next();
                if ((put == null || put == pt.getType().getBasic()) && pt.getUuid().equals(target)) {
                    if (!current || !pt.isExpired()) {
                        ptList.add(pt);
                    } else {
                        pt.delete(null, false, false);
                        iterator.remove();
                    }
                }
            }
        } else {
            try (ResultSet rs = DatabaseManager.get().executeResultStatement(current ? SQLQuery.SELECT_USER_PUNISHMENTS : SQLQuery.SELECT_USER_PUNISHMENTS_HISTORY, target)) {
                while (rs.next()) {
                    Punishment punishment = getPunishmentFromResultSet(rs);
                    if ((put == null || put == punishment.getType().getBasic()) && (!current || !punishment.isExpired())) {
                        ptList.add(punishment);
                    }
                }
            } catch (SQLException ex) {
                universal.log("An error has occurred getting the punishments for " + target);
                universal.debug(ex);
            }
        }
        return ptList;
    }

    /**
     * Get parsed punishments from the database queried by the given {@link SQLQuery}.<br>
     * The parameters work as described in {@link MessageManager#sendMessage(Object, String, boolean, String...)}.
     *
     * @param sqlQuery   the sql query
     * @param parameters the parameters
     * @return the punishments
     */
    public List<Punishment> getPunishments(SQLQuery sqlQuery, Object... parameters) {
        List<Punishment> ptList = new ArrayList<>();

        ResultSet rs = DatabaseManager.get().executeResultStatement(sqlQuery, parameters);
        try {
            while (rs.next()) {
                Punishment punishment = getPunishmentFromResultSet(rs);
                ptList.add(punishment);
            }
            rs.close();
        } catch (SQLException ex) {
            universal.log("An error has occurred executing a query in the database.");
            universal.debug("Query: \n" + sqlQuery);
            universal.debug(ex);
        }
        return ptList;
    }

    /**
     * Get an active punishment by id.
     *
     * @param id the id
     * @return the punishment
     */
    public Punishment getPunishment(int id) {
        ResultSet rs = DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_PUNISHMENT_BY_ID, id);
        Punishment pt = null;
        try {
            if (rs.next()) {
                pt = getPunishmentFromResultSet(rs);
            }
            rs.close();
        } catch (SQLException ex) {
            universal.log("An error has occurred getting a punishment by his id.");
            universal.debug("Punishment id: '" + id + "'");
            universal.debug(ex);
        }
        return pt == null || pt.isExpired() ? null : pt;
    }

    /**
     * Get an active warning by id.
     *
     * @param id the id
     * @return the warning
     */
    public Punishment getWarn(int id) {
        Punishment punishment = getPunishment(id);

        if(punishment == null)
            return null;

        return punishment.getType().getBasic() == PunishmentType.WARNING ? punishment : null;
    }

    /**
     * Get a players active warnings.
     *
     * @param uuid the players uuid
     * @return the warns
     */
    public List<Punishment> getWarns(String uuid) {
        return getPunishments(uuid, PunishmentType.WARNING, true);
    }

    /**
     * Get a players active ban.
     *
     * @param uuid the players uuid (can also be an IP)
     * @return the ban or <code>null</code> if not banned
     */
    public Punishment getBan(String uuid) {
        List<Punishment> punishments = getPunishments(uuid, PunishmentType.BAN, true);
        return punishments.isEmpty() ? null : punishments.get(0);
    }

    /**
     * Get a players active mute.
     *
     * @param uuid the players uuid
     * @return the mute or <code>null</code> if not muted
     */
    public Punishment getMute(String uuid) {
        List<Punishment> punishments = getPunishments(uuid, PunishmentType.MUTE, true);
        return punishments.isEmpty() ? null : punishments.get(0);
    }

    /**
     * Check whether a player is banned.
     *
     * @param uuid the players uuid (can also be an IP)
     * @return whether the player is banned
     */
    public boolean isBanned(String uuid) {
        return getBan(uuid) != null;
    }

    /**
     * Check whether a player is muted.
     *
     * @param uuid the players uuid
     * @return whether the player is muted
     */
    public boolean isMuted(String uuid) {
        return getMute(uuid) != null;
    }

    /**
     * Check whether the data for the given uuid, ip or username are currently cached.
     *
     * @param target the target (uuid, ip or username)
     * @return whether the targets data is cached
     */
    public boolean isCached(String target) {
        return cached.contains(target);
    }

    /**
     * Mark the InterimData as cached.
     * This method des not acually cache the data, see {@link InterimData#accept()} to do that.
     *
     * @param data the data
     */
    public void setCached(InterimData data) {
        cached.add(data.getName());
        cached.add(data.getIp());
        cached.add(data.getUuid());
    }

    /**
     * Get punishment-time calculation level.
     * This level is represented by the amount a user has been punished using the given time-layout.
     *
     * @param uuid   the players uuid
     * @param layout the time-layout name
     * @return the calculation level
     */
    public int getCalculationLevel(String uuid, String layout) {
        if (isCached(uuid)) {
            return (int) history.stream().filter(pt -> pt.getUuid().equals(uuid) && layout.equalsIgnoreCase(pt.getCalculation())).count();
        }
        
        int i = 0;
        try (ResultSet resultSet = DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_USER_PUNISHMENTS_HISTORY_BY_CALCULATION, uuid, layout)) {
        	
            while (resultSet.next()) {
            	i++;
            }
            
        } catch (SQLException ex) {
            universal.log("An error has occurred getting the level for the layout '" + layout + "' for '" + uuid + "'");
            universal.debug(ex);
        }
        return i;
    }

    /**
     * Get how many warnings a player has.
     *
     * @param uuid the players uuid
     * @return the current warning count
     */
    public int getCurrentWarns(String uuid) {
        return getWarns(uuid).size();
    }

    /**
     * Get all cached punishments.
     *
     * @param checkExpired whether to look for and remove expired punishments
     * @return the cached punishments
     */
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

    /**
     * Get a Punishment from a {@link ResultSet}
     *
     * @param rs the result set
     * @return the punishment from the result set
     * @throws SQLException the sql exception
     */
    public Punishment getPunishmentFromResultSet(ResultSet rs) throws SQLException {
        return new Punishment(
                rs.getString("name"),
                rs.getString("uuid"), rs.getString("reason"),
                rs.getString("operator"),
                PunishmentType.valueOf(rs.getString("punishmentType")),
                rs.getLong("start"),
                rs.getLong("end"),
                rs.getString("calculation"),
                rs.getInt("id"));
    }

    /**
     * Get all cached history punishments.
     *
     * @return the loaded history
     */
    public Set<Punishment> getLoadedHistory() {
        return history;
    }


//    public long getCalculation(String layout, String name, String uuid) {
//        long end = TimeManager.getTime();
//        MethodInterface mi = Universal.get().getMethods();
//
//        int i = getCalculationLevel(name, uuid);
//
//        List<String> timeLayout = mi.getStringList(mi.getLayouts(), "Time." + layout);
//        String time = timeLayout.get(timeLayout.size() <= i ? timeLayout.size() - 1 : i);
//        long toAdd = TimeManager.toMilliSec(time.toLowerCase());
//        end += toAdd;
//
//        return end;
//    }
}