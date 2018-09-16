package me.leoko.advancedban.punishment;

import lombok.Data;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanPlayer;
import me.leoko.advancedban.utils.SQLQuery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by Leoko @ dev.skamps.eu on 30.05.2016.
 */
@Data
public class Punishment {

    private final AdvancedBan advancedBan;
    private final Object identifier;
    private final String name;
    private final String operator;
    private final String calculation;
    private final long start;
    private final long end;
    private final PunishmentType type;

    private String reason;
    private int id = -1;

    public String getReason() {
        return (reason == null ? advancedBan.getConfiguration().getDefaultReason() : reason).replaceAll("'", "");
    }

    public String getHexId() {
        return Integer.toHexString(id).toUpperCase();
    }

    public String getDate(long date) {
        SimpleDateFormat format = new SimpleDateFormat(advancedBan.getConfiguration().getDateFormat());
        return format.format(new Date(date));
    }

    public void create() {
        create(false);
    }

    public void create(boolean silent) {
        if (id != -1) {
            advancedBan.getLogger().warn("Unable to overwrite the punishment:\n" + toString());
            return;
        }

        if (identifier == null) {
            advancedBan.getLogger().warn("No Identifier was found for punishment on: " + type.getName());
            return;
        }

        advancedBan.getDatabaseManager().executeStatement(SQLQuery.INSERT_PUNISHMENT_HISTORY, name, identifier.toString(), getReason(), operator, type.name(), start, end, calculation);

        if (getType() != PunishmentType.KICK) {
            try {
                advancedBan.getDatabaseManager().executeStatement(SQLQuery.INSERT_PUNISHMENT, name, identifier.toString(), getReason(), operator, type.name(), start, end, calculation);
                ResultSet rs = advancedBan.getDatabaseManager().executeResultStatement(SQLQuery.SELECT_EXACT_PUNISHMENT, identifier.toString(), start);
                if (rs.next()) {
                    id = rs.getInt("id");
                } else {
                    advancedBan.getLogger().warn("Not able to update ID of punishment! Please restart the server to resolve this issue!\n" + toString());
                }
                rs.close();
            } catch (SQLException ex) {
                advancedBan.getLogger().logException(ex);
            }
        }

        final int cWarnings = getType().getBasic() == PunishmentType.WARNING ? (advancedBan.getPunishmentManager().getCurrentWarns(identifier) + 1) : 0;

        if (getType().getBasic() == PunishmentType.WARNING) {
            Optional<String> command = Optional.empty();
            for (int i = 1; i <= cWarnings; i++) {
                String action = advancedBan.getConfiguration().getWarnActions().get(i);
                if (action != null) {
                    command = Optional.of(action);
                }
            }
            command.ifPresent(cmd -> {
                final String finalCmd = cmd.replaceAll("%PLAYER%", getName()).replaceAll("%COUNT%", cWarnings + "").replaceAll("%REASON%", getReason());
                advancedBan.runSyncTask(() -> {
                    advancedBan.executeCommand(finalCmd);
                    advancedBan.getLogger().info("Executed command: " + finalCmd);
                });
            });
        }

        if (!silent) {
            announce(cWarnings);
        }

        Optional<AdvancedBanPlayer> player = advancedBan.getPlayer(identifier.toString());

        if (player.isPresent()) {
            if (getType().getBasic() == PunishmentType.BAN || getType() == PunishmentType.KICK) {
                advancedBan.runSyncTask(() -> player.get().kick(getLayoutBSN()));
            } else {
                for (String str : getLayout()) {
                    player.get().sendMessage(str);
                }
                advancedBan.getPunishmentManager().getLoadedPunishments(false).add(this);
            }
        }

        advancedBan.getPunishmentManager().getLoadedHistory().add(this);

        advancedBan.callPunishmentEvent(this);
    }

    public void updateReason(String reason) {
        this.reason = reason;

        if (id != -1) {
            advancedBan.getDatabaseManager().executeStatement(SQLQuery.UPDATE_PUNISHMENT_REASON, reason, id);
        }
    }

    private void announce(int cWarnings) {
        List<String> notification = advancedBan.getMessageManager().getMessageList(getType().getConfSection() + ".Notification",
                "OPERATOR", getOperator(),
                "PREFIX", advancedBan.getConfiguration().isPrefixDisabled() ? "" : advancedBan.getMessageManager().getMessage("General.Prefix"),
                "DURATION", getDuration(true),
                "REASON", getReason(),
                "NAME", getName(),
                "ID", String.valueOf(id),
                "HEXID", getHexId(),
                "DATE", getDate(start),
                "COUNT", cWarnings + "");

        advancedBan.notify("ab." + getType().getName() + ".notify", notification);
    }

    public void delete() {
        delete(null, false, true);
    }

    public void delete(String who, boolean massClear, boolean removeCache) {
        if (getType() == PunishmentType.KICK) {
            advancedBan.getLogger().warn("You are not able to delete kicks!");
            return;
        }

        if (id == -1) {
            advancedBan.getLogger().warn("The Punishment has not yet been created!\n" + toString());
            return;
        }

        advancedBan.getDatabaseManager().executeStatement(SQLQuery.DELETE_PUNISHMENT, getId());

        if (removeCache) {
            advancedBan.getPunishmentManager().getLoadedPunishments(false).remove(this);
        }

        if (who != null) {
            advancedBan.getLogger().debug(who + " is deleting a punishment");
        }
        advancedBan.getLogger().debug("Deleted punishment " + getId() + " from " + getName() + " punishment reason: " + getReason());
        advancedBan.callRevokePunishmentEvent(this, massClear);
    }

    public List<String> getLayout() {
        if (getReason().startsWith("@") || getReason().startsWith("~")) {
            return advancedBan.getMessageManager().getLayout(
                    "Message." + getReason().split(" ")[0].substring(1),
                    "OPERATOR", getOperator(),
                    "PREFIX", advancedBan.getConfiguration().isPrefixDisabled() ? "" : advancedBan.getMessageManager().getMessage("General.Prefix"),
                    "DURATION", getDuration(false),
                    "REASON", getReason().split(" ").length < 2 ? "" : getReason().substring(getReason().split(" ")[0].length() + 1),
                    "HEXID", getHexId(),
                    "ID", String.valueOf(id),
                    "DATE", getDate(start),
                    "COUNT", getType().getBasic() == PunishmentType.WARNING ? (advancedBan.getPunishmentManager().getCurrentWarns(getIdentifier()) + 1) + "" : "0"
            );
        } else {
            return advancedBan.getMessageManager().getMessageList(
                    getType().getConfSection() + ".Layout",
                    "OPERATOR", getOperator(),
                    "PREFIX", advancedBan.getConfiguration().isPrefixDisabled() ? "" : advancedBan.getMessageManager().getMessage("General.Prefix"),
                    "DURATION", getDuration(false),
                    "REASON", getReason(),
                    "HEXID", getHexId(),
                    "ID", String.valueOf(id),
                    "DATE", getDate(start),
                    "COUNT", getType().getBasic() == PunishmentType.WARNING ? (advancedBan.getPunishmentManager().getCurrentWarns(getIdentifier()) + 1) + "" : "0"
            );
        }
    }

    public String getDuration(boolean fromStart) {
        String duration = "permanent";
        if (getType().isTemp()) {
            long diff = (getEnd() - (fromStart ? start : advancedBan.getTimeManager().getTime())) / 1000;
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

    private String[] getDurationParameter(String... parameter) {
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

    public String getLayoutBSN() {
        StringBuilder msg = new StringBuilder();
        for (String str : getLayout()) {
            msg.append("\n").append(str);
        }
        return msg.substring(1);
    }

    public boolean isExpired() {
        return getType().isTemp() && getEnd() <= advancedBan.getTimeManager().getTime();
    }
}
