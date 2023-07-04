package me.leoko.advancedban.manager;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.utils.InterimData;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;
import me.leoko.advancedban.utils.SQLQuery;
import me.leoko.advancedban.utils.UncheckedSQLException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;


/**
 * The Punishment Manager handles the punishments. It loads and parses them from the database, caches them
 * and eventually discards them again.
 */
public class PunishmentManager {

    private static PunishmentManager instance = null;
    private final Set<Punishment> punishments = Collections.synchronizedSet(new HashSet<>());
    private final Set<Punishment> history = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> cached = Collections.synchronizedSet(new HashSet<>());
    
    private Universal universal() {
    	return Universal.get();
    }

    /**
     * Get the punishment manager.
     *
     * @return the punishment manager instance
     */
    public static synchronized PunishmentManager get() {
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
            if (resultsHistory == null || resultsPunishments == null)
                return null;

            while (resultsPunishments.next()) {
                punishments.add(getPunishmentFromResultSet(resultsPunishments));
            }
            while (resultsHistory.next()) {
                history.add(getPunishmentFromResultSet(resultsHistory));
            }

        } catch (SQLException ex) {
            throw new UncheckedSQLException("While loading punishments from the database", ex);
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
        return getPunishmentsOfTypes(target, put == null ? Arrays.asList(PunishmentType.values()) : Arrays.asList(put), current);
    }

    /**
     * Get all punishments which belong to the given uuid or ip.
     *
     * @param target  the uuid or ip to search for
     * @param putList a List of the basic punishment type to search for ({@link PunishmentType#BAN} would also include Tempbans).
     * @param current if only active punishments should be included.
     * @return the punishments
     */
    public List<Punishment> getPunishmentsOfTypes(String target, List<PunishmentType> putList, boolean current) {
        List<Punishment> ptList = new ArrayList<>();

        if (isCached(target)) {
            for (Iterator<Punishment> iterator = (current ? punishments : history).iterator(); iterator.hasNext(); ) {
                Punishment pt = iterator.next();
                if (putList.contains(pt.getType().getBasic()) && pt.getUuid().equals(target)) {
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
                    if (putList.contains(punishment.getType().getBasic()) && (!current || !punishment.isExpired())) {
                        ptList.add(punishment);
                    }
                }
            } catch (SQLException ex) {
                throw new UncheckedSQLException("While getting the punishments for " + target, ex);
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
            throw new UncheckedSQLException("While getting punishments from query " + sqlQuery + " with parameters "
                    + Arrays.toString(parameters), ex);
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
        final Optional<Punishment> cachedPunishment = getLoadedPunishments(false).stream()
                .filter(punishment -> punishment.getId() == id).findAny();

        if (cachedPunishment.isPresent())
            return cachedPunishment.get();


        try (ResultSet rs = DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_PUNISHMENT_BY_ID, id)) {
            if (rs.next()) {
                final Punishment punishment = getPunishmentFromResultSet(rs);
                if (!punishment.isExpired())
                    return punishment;
            }
        } catch (SQLException ex) {
            throw new UncheckedSQLException("While getting a punishment by its id " + id, ex);
        }

        return null;
    }

    /**
     * Get an active warning by id.
     *
     * @param id the id
     * @return the warning
     */
    public Punishment getWarn(int id) {
        Punishment punishment = getPunishment(id);

        if (punishment == null)
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
    } /**
     * Get an active note by id.
    *
    * @param id the id
    * @return the note
    */
   public Punishment getNote(int id) {
       Punishment punishment = getPunishment(id);

       if (punishment == null)
           return null;

       return punishment.getType().getBasic() == PunishmentType.NOTE ? punishment : null;
   }

   /**
    * Get a players active note.
    *
    * @param uuid the players uuid
    * @return the note
    */
   public List<Punishment> getNotes(String uuid) {
       return getPunishments(uuid, PunishmentType.NOTE, true);
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
            throw new UncheckedSQLException("While getting the level for the layout '" + layout + "' for '" + uuid + "'", ex);
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
     * Get how many notes a player has.
     *
     * @param uuid the players uuid
     * @return the current note count
     */
    public int getCurrentNotes(String uuid) {
        return getNotes(uuid).size();
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