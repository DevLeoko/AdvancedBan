package me.leoko.advancedban.punishment;

import lombok.RequiredArgsConstructor;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanPlayer;
import me.leoko.advancedban.utils.SQLQuery;

import javax.annotation.Nonnull;
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

    private static String[] getDurationParameter(String... parameter) {
        int length = parameter.length;
        String[] newParameter = new String[length * 2];
        for (int i = 0; i < length; i += 2) {
            String name = parameter[i];
            String count = parameter[i + 1];

            newParameter[i] = name;
            newParameter[i + 1] = count;
            newParameter[length + i] = name + name;
            newParameter[length + i + 1] = (count.length() <= 1 ? "0" : "") + count;
        }

        return newParameter;
    }

    public InterimData load(@Nonnull UUID uuid, @Nonnull String name, @Nonnull InetAddress address) {
        Objects.requireNonNull(uuid, "uuid");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(address, "address");
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

    public Optional<Punishment> getInterimBan(@Nonnull InterimData data) {
        Objects.requireNonNull(data, "data");
        for (Punishment pt : data.getPunishments()) {
            if (pt.getType().getBasic() == PunishmentType.BAN && !isExpired(pt)) {
                return Optional.of(pt);
            }
        }
        return Optional.empty();
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

    public void acceptData(@Nonnull InterimData data) {
        Objects.requireNonNull(data, "data");
        getLoadedPunishments(false).addAll(data.getPunishments());
        getLoadedHistory().addAll(data.getHistory());
        addCached(data.getName());
        addCached(data.getAddress());
        addCached(data.getUuid());
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

    public List<Punishment> getPunishments(Object identifier, PunishmentType type, boolean current) {
        List<Punishment> punishments = new ArrayList<>();

        if (isCached(identifier)) {
            for (Iterator<Punishment> iterator = (current ? this.punishments : history).iterator(); iterator.hasNext(); ) {
                Punishment punishment = iterator.next();
                if ((type == null || type == punishment.getType().getBasic()) && punishment.getIdentifier().equals(identifier)) {
                    if (!current || !isExpired(punishment)) {
                        punishments.add(punishment);
                    } else {
                        deletePunishment(punishment, false);
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
                    if ((type == null || type == punishment.getType().getBasic()) && (!current || !isExpired(punishment))) {
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

    public Optional<Punishment> getWarn(int id) {
        Optional<Punishment> punishment = getPunishment(id);
        return punishment.isPresent() && punishment.get().getType().getBasic() == PunishmentType.WARNING ? punishment : Optional.empty();
    }

    public List<Punishment> getWarns(Object object) {
        return getPunishments(object, PunishmentType.WARNING, true);
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
        return pt == null || isExpired(pt) ? Optional.empty() : Optional.of(pt);
    }

    public Optional<Punishment> getMute(Object object) {
        List<Punishment> punishments = getPunishments(object, PunishmentType.MUTE, true);
        return punishments.isEmpty() ? Optional.empty() : Optional.ofNullable(punishments.get(0));
    }

    public Optional<Punishment> getInterimBan(Object object) {
        List<Punishment> punishments = getPunishments(object, PunishmentType.BAN, true);
        return punishments.isEmpty() ? Optional.empty() : Optional.ofNullable(punishments.get(0));
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

    public boolean isBanned(Object object) {
        return getInterimBan(object).isPresent();
    }

    public Set<Punishment> getLoadedPunishments(boolean checkExpired) {
        if (checkExpired) {
            List<Punishment> toDelete = new ArrayList<>();
            for (Punishment pu : punishments) {
                if (isExpired(pu)) {
                    toDelete.add(pu);
                }
            }
            for (Punishment pu : toDelete) {
                deletePunishment(pu);
            }
        }
        return punishments;
    }

    public Punishment getPunishmentFromResultSet(ResultSet rs) throws SQLException {
        Punishment punishment = new Punishment(
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

    public long getCalculation(String layout, String name, String uuid) {
        long end = advancedBan.getTimeManager().getTime();

        int i = getCalculationLevel(name, uuid);

        List<String> timeLayout = advancedBan.getMessageManager().getLayout("Time." + layout);
        String time = timeLayout.get(timeLayout.size() <= i ? timeLayout.size() - 1 : i);
        long toAdd = advancedBan.getTimeManager().toMilliSec(time.toLowerCase());
        end += toAdd;

        return end;
    }

    public void updatePunishment(@Nonnull Punishment punishment) {
        Objects.requireNonNull(punishment, "punishment");
        if (!punishment.getId().isPresent()) throw new IllegalArgumentException("Punishment is not registered");

        advancedBan.getDatabaseManager().executeStatement(SQLQuery.UPDATE_PUNISHMENT_REASON,
                punishment.getReason().orElse(null), punishment.getId().getAsInt());
    }

    public void addPunishment(@Nonnull Punishment punishment) {
        addPunishment(punishment, false);
    }

    public void addPunishment(@Nonnull Punishment punishment, boolean silent) {
        Objects.requireNonNull(punishment, "punishment");
        if (punishment.getId().isPresent()) {
            throw new IllegalArgumentException("Punishment has already been added");
        }

        advancedBan.getDatabaseManager().executeStatement(
                SQLQuery.INSERT_PUNISHMENT_HISTORY,
                punishment.getName(),
                punishment.getIdentifier().toString(),
                punishment.getReason().orElse(null),
                punishment.getOperator(),
                punishment.getType().name(),
                punishment.getStart(),
                punishment.getEnd(),
                punishment.getCalculation()
        );

        if (punishment.getType() != PunishmentType.KICK) {
            ResultSet rs;
            try {
                advancedBan.getDatabaseManager().executeStatement(
                        SQLQuery.INSERT_PUNISHMENT,
                        punishment.getName(),
                        punishment.getIdentifier().toString(),
                        punishment.getReason().orElse(null),
                        punishment.getOperator(),
                        punishment.getType().name(),
                        punishment.getStart(),
                        punishment.getEnd(),
                        punishment.getCalculation()
                );
                rs = advancedBan.getDatabaseManager().executeResultStatement(SQLQuery.SELECT_EXACT_PUNISHMENT,
                        punishment.getIdentifier().toString(), punishment.getStart());
                if (rs.next()) {
                    punishment.setId(rs.getInt("id"));
                } else {
                    advancedBan.getLogger().warn("Not able to update ID of punishment! Please restart the server to resolve this issue!\n" + toString());
                }
                rs.close();
            } catch (SQLException ex) {
                advancedBan.getLogger().logException(ex);
            }
        }

        final int cWarnings = punishment.getType().getBasic() == PunishmentType.WARNING ?
                (advancedBan.getPunishmentManager().getCurrentWarns(punishment.getIdentifier()) + 1) : 0;

        if (punishment.getType().getBasic() == PunishmentType.WARNING) {
            Optional<String> command = Optional.empty();
            for (int i = 1; i <= cWarnings; i++) {
                String action = advancedBan.getConfiguration().getWarnActions().get(i);
                if (action != null) {
                    command = Optional.of(action);
                }
            }
            command.ifPresent(cmd -> {
                final String finalCmd = cmd.replaceAll("%PLAYER%", punishment.getName())
                        .replaceAll("%COUNT%", cWarnings + "")
                        .replaceAll("%REASON%", punishment.getReason().orElse(advancedBan.getConfiguration().getDefaultReason()));
                advancedBan.runSyncTask(() -> {
                    advancedBan.executeCommand(finalCmd);
                    advancedBan.getLogger().info("Executed command: " + finalCmd);
                });
            });
        }

        if (!silent) {
            announce(punishment, cWarnings);
        }

        Optional<AdvancedBanPlayer> player = advancedBan.getPlayer(punishment.getIdentifier().toString());

        if (player.isPresent()) {
            if (punishment.getType().getBasic() == PunishmentType.BAN || punishment.getType() == PunishmentType.KICK) {
                advancedBan.runSyncTask(() -> player.get().kick(getLayoutBSN(punishment)));
            } else {
                for (String str : getLayout(punishment)) {
                    player.get().sendMessage(str);
                }
                advancedBan.getPunishmentManager().getLoadedPunishments(false).add(punishment);
            }
        }

        advancedBan.getPunishmentManager().getLoadedHistory().add(punishment);

        advancedBan.callPunishmentEvent(punishment);
    }

    public void deletePunishment(@Nonnull Punishment punishment) {
        deletePunishment(punishment, false);
    }

    public void deletePunishment(@Nonnull Punishment punishment, boolean massClear) {
        Objects.requireNonNull(punishment, "punishment");
        if (!punishment.getId().isPresent()) {
            throw new IllegalArgumentException("Punishment has not been added");
        }

        advancedBan.getDatabaseManager().executeStatement(SQLQuery.DELETE_PUNISHMENT, punishment.getId().getAsInt());

        getLoadedPunishments(false).remove(punishment);

        advancedBan.getLogger().debug("Deleted punishment " + punishment.getId().getAsInt() + " from " +
                punishment.getName() + " punishment reason: " +
                punishment.getReason().orElse(advancedBan.getConfiguration().getDefaultReason()));
        advancedBan.callRevokePunishmentEvent(punishment, massClear);
    }

    public String getDuration(@Nonnull Punishment punishment, boolean fromStart) {
        Objects.requireNonNull(punishment, "punishment");
        String duration = "permanent";
        if (punishment.getType().isTemp()) {
            long diff = (punishment.getEnd() - (fromStart ? punishment.getStart() : advancedBan.getTimeManager().getTime())) / 1000;
            if (diff > 60 * 60 * 24) {
                duration = advancedBan.getMessageManager().getMessage("General.TimeLayoutD", getDurationParameter("D", diff / 60 / 60 / 24 + "", "H", diff / 60 / 60 % 24 + "", "M", diff / 60 % 60 + "", "S", diff % 60 + ""));
            } else if (diff > 60 * 60) {
                duration = advancedBan.getMessageManager().getMessage("General.TimeLayoutH", getDurationParameter("H", diff / 60 / 60 + "", "M", diff / 60 % 60 + "", "S", diff % 60 + ""));
            } else if (diff > 60) {
                duration = advancedBan.getMessageManager().getMessage("General.TimeLayoutM", getDurationParameter("M", diff / 60 + "", "S", diff % 60 + ""));
            } else {
                duration = advancedBan.getMessageManager().getMessage("General.TimeLayoutS", getDurationParameter("S", diff + ""));
            }
        }
        return duration;
    }

    private void announce(Punishment punishment, int cWarnings) {
        List<String> notification = advancedBan.getMessageManager().getMessageList(punishment.getType().getConfSection() + ".Notification",
                "OPERATOR", punishment.getOperator(),
                "PREFIX", advancedBan.getConfiguration().isPrefixDisabled() ? "" : advancedBan.getMessageManager().getMessage("General.Prefix"),
                "DURATION", getDuration(punishment, true),
                "REASON", advancedBan.getMessageManager().getReasonOrDefault(punishment.getReason()),
                "NAME", punishment.getName(),
                "ID", String.valueOf(punishment.getId().orElse(-1)),
                "HEXID", Integer.toHexString(punishment.getId().orElse(-1)).toUpperCase(),
                "DATE", advancedBan.getTimeManager().getDate(punishment.getStart()),
                "COUNT", cWarnings + "");

        advancedBan.notify("ab." + punishment.getType().getName() + ".notify", notification);
    }

    public List<String> getLayout(@Nonnull Punishment punishment) {
        Objects.requireNonNull(punishment, "punishment");

        String operator = punishment.getOperator();
        String prefix = advancedBan.getMessageManager().getPrefix();
        String duration = getDuration(punishment, false);
        String hexId = Integer.toHexString(punishment.getId().orElse(-1)).toUpperCase();
        String id = Integer.toString(punishment.getId().orElse(-1));
        String date = advancedBan.getTimeManager().getDate(punishment.getStart());
        String count = punishment.getType().getBasic() == PunishmentType.WARNING ?
                (advancedBan.getPunishmentManager().getCurrentWarns(punishment.getIdentifier()) + 1) + "" : "0";

        if (punishment.getReason().isPresent() &&
                (punishment.getReason().get().startsWith("@") || punishment.getReason().get().startsWith("~"))) {
            return advancedBan.getMessageManager().getLayout(
                    "Message." + punishment.getReason().get().split(" ")[0].substring(1),
                    "OPERATOR", operator,
                    "PREFIX", prefix,
                    "DURATION", duration,
                    "REASON", punishment.getReason().get().split(" ").length < 2 ? "" :
                            punishment.getReason().get().substring(punishment.getReason().get().split(" ")[0].length() + 1),
                    "HEXID", hexId,
                    "ID", id,
                    "DATE", date,
                    "COUNT", count
            );
        } else {
            return advancedBan.getMessageManager().getMessageList(
                    punishment.getType().getConfSection() + ".Layout",
                    "OPERATOR", operator,
                    "PREFIX", prefix,
                    "DURATION", duration,
                    "REASON", advancedBan.getMessageManager().getReasonOrDefault(punishment.getReason()),
                    "HEXID", hexId,
                    "ID", id,
                    "DATE", date,
                    "COUNT", count
            );
        }
    }

    public String getLayoutBSN(@Nonnull Punishment punishment) {
        StringBuilder msg = new StringBuilder();
        for (String str : getLayout(punishment)) {
            msg.append("\n").append(str);
        }
        return msg.substring(1);
    }

    public boolean isExpired(@Nonnull Punishment punishment) {
        Objects.requireNonNull(punishment, "punishment");
        return punishment.getType().isTemp() && punishment.getEnd() <= advancedBan.getTimeManager().getTime();
    }
}