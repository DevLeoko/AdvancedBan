package me.leoko.advancedban.utils;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.MessageManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.TimeManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Leoko @ dev.skamps.eu on 30.05.2016.
 */
public class Punishment {
    private static final MethodInterface mi = Universal.get().getMethods();
    private final String name;
    private final String uuid;
    private final String reason;
    private final String operator;
    private final long start;
    private final long end;
    private final String calculation;
    private final PunishmentType type;
    private int id;

    public Punishment(String name, String uuid, String reason, String operator, PunishmentType type, long start, long end, String calculation, int id) {
        this.name = name;
        this.uuid = uuid;
        this.reason = reason;
        this.operator = operator;
        this.type = type;
        this.start = start;
        this.end = end;
        this.calculation = calculation;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public String getReason() {
        return (reason == null ? mi.getString(mi.getConfig(), "DefaultReason", "none") : reason).replaceAll("'", "");
    }

    public String getOperator() {
        return operator;
    }

    public String getCalculation() {
        return calculation;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public PunishmentType getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public void create() {
        if (id != -1) {
            System.out.println("!! Failed! AB tried to overwrite the punishment:");
            System.out.println("!! Failed at: " + toString());
            return;
        }

        if (uuid == null) {
            System.out.println("!! Failed! AB has not saved the " + getType().getName() + " because there is no fetched UUID");
            System.out.println("!! Failed at: " + toString());
            return;
        }

        if (Universal.get().isUseMySQL()) {
            if (getType() != PunishmentType.KICK)
                Universal.get().getMysql().executeStatement("INSERT INTO `Punishments` (`name`, `uuid`, `reason`, `operator`, `punishmentType`, `start`, `end`, `calculation`) VALUES ('" + getName() + "', '" + getUuid() + "', '" + getReason() + "', '" + getOperator() + "', '" + getType().name() + "', '" + getStart() + "', '" + getEnd() + "', '" + getCalculation() + "')");
            Universal.get().getMysql().executeStatement("INSERT INTO `PunishmentHistory` (`name`, `uuid`, `reason`, `operator`, `punishmentType`, `start`, `end`, `calculation`) VALUES ('" + getName() + "', '" + getUuid() + "', '" + getReason() + "', '" + getOperator() + "', '" + getType().name() + "', '" + getStart() + "', '" + getEnd() + "', '" + getCalculation() + "')");
            ResultSet rs = Universal.get().getMysql().executeRespStatement("SELECT * FROM `Punishments` WHERE `uuid` = '" + getUuid() + "' AND `start` = '" + getStart() + "'");

            if (getType() != PunishmentType.KICK) {
                try {
                    if (rs.next()) {
                        id = rs.getInt("id");
                    } else {
                        System.out.println("!! No able to update ID of punishment! Please restart the server to resolve this issue!");
                        System.out.println("!! Failed at: " + toString());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else { //TODO improve performance!

            int i = 1;
            while (mi.contains(mi.getData(), "Punishments." + i)) i++;
            id = i;

            String pathT = "Punishments.";
            for (int j = 0; j < 2; j++) {
                if (j != 0 || getType() != PunishmentType.KICK) {
                    mi.set(mi.getData(), pathT + id + ".name", getName());
                    mi.set(mi.getData(), pathT + id + ".uuid", getUuid());
                    mi.set(mi.getData(), pathT + id + ".reason", getReason());
                    mi.set(mi.getData(), pathT + id + ".operator", getOperator());
                    mi.set(mi.getData(), pathT + id + ".punishmentType", getType().name());
                    mi.set(mi.getData(), pathT + id + ".start", getStart());
                    mi.set(mi.getData(), pathT + id + ".end", getEnd());
                    mi.set(mi.getData(), pathT + id + ".calculation", getCalculation());
                }
                pathT = "PunishmentHistory.";
            }

            mi.saveData();
        }


        final int cWarnings = getType().getBasic() == PunishmentType.WARNING ? (PunishmentManager.get().getCurrentWarns(getUuid()) + 1) : 0;

        if (getType().getBasic() == PunishmentType.WARNING) {
            String cmd = "";
            for (int i = 1; i <= cWarnings; i++) {
                if (mi.contains(mi.getConfig(), "WarnActions." + i))
                    cmd = mi.getString(mi.getConfig(), "WarnActions." + i);
            }
            final String finalCmd = cmd.replaceAll("%PLAYER%", getName()).replaceAll("%COUNT%", cWarnings + "").replaceAll("%REASON%", getReason());
            mi.runSync(() -> {
                mi.executeCommand(finalCmd);
                System.out.println("[AdvancedBan] Executing command: " + finalCmd);
            });
        }

        List<String> notification = MessageManager.getLayout(mi.getMessages(),
                getType().getConfSection() + ".Notification",
                "OPERATOR", getOperator(),
                "PREFIX", MessageManager.getMessage("General.Prefix"),
                "DURATION", getDuration(true),
                "REASON", getReason(),
                "NAME", getName(),
                "COUNT", cWarnings + ""
        );

        for (Object op : mi.getOnlinePlayers()) {
            if (mi.hasPerms(op, "ab." + getType().getName() + ".notify"))
                for (String str : notification)
                    mi.sendMessage(op, str);
        }

        if (mi.isOnline(getName())) {
            final Object p = mi.getPlayer(getName());

            if (getType().getBasic() == PunishmentType.BAN || getType() == PunishmentType.KICK) {
                mi.runSync(() -> mi.kickPlayer(p, getLayoutBSN()));
            } else {
                for (String str : getLayout()) mi.sendMessage(p, str);
            }
        }

        if (getType() != PunishmentType.KICK) PunishmentManager.get().getPunishments(false).add(this);
        PunishmentManager.get().getHistory().add(this);
    }

    public void delete() {
        if (getType() == PunishmentType.KICK) {
            System.out.println("!! Failed deleting! You are not able to delete Kicks!");
        }

        if (id == -1) {
            System.out.println("!! Failed deleting! The Punishment is not created yet!");
            System.out.println("!! Failed at: " + toString());
            return;
        }


        if (Universal.get().isUseMySQL()) {
            Universal.get().getMysql().executeStatement("DELETE FROM `Punishments` WHERE `id` = " + getId());
        } else {
            mi.set(mi.getData(), "Punishments." + getId(), null);
            mi.saveData();
        }

//        if(PunishmentManager.get().getPunishments().contains(this))
        PunishmentManager.get().getPunishments(false).remove(this);
    }

    public List<String> getLayout() {
        boolean isLayout = getReason().matches("@.+") || getReason().matches("~.+");

        return MessageManager.getLayout(
                isLayout ? mi.getLayouts() : mi.getMessages(),
                isLayout ? "Message." + getReason().substring(1) : getType().getConfSection() + ".Layout",
                "OPERATOR", getOperator(),
                "PREFIX", MessageManager.getMessage("General.Prefix"),
                "DURATION", getDuration(false),
                "REASON", getReason(),
                "COUNT", getType().getBasic() == PunishmentType.WARNING ? (PunishmentManager.get().getCurrentWarns(getUuid()) + 1) + "" : "0"
        );
    }

    public String getDuration(boolean fromStart) {
        String duration = "permanent";
        if (getType().isTemp()) {
            long diff = (getEnd() - (fromStart ? start : TimeManager.getTime())) / 1000;
            if (diff > 60 * 60 * 24)
                duration = MessageManager.getMessage("General.TimeLayoutD", "D", diff / 60 / 60 / 24 + "", "H", diff / 60 / 60 % 24 + "", "M", diff / 60 % 60 + "", "S", diff % 60 + "");
            else if (diff > 60 * 60)
                duration = MessageManager.getMessage("General.TimeLayoutH", "H", diff / 60 / 60 + "", "M", diff / 60 % 60 + "", "S", diff % 60 + "");
            else if (diff > 60)
                duration = MessageManager.getMessage("General.TimeLayoutM", "M", diff / 60 + "", "S", diff % 60 + "");
            else duration = MessageManager.getMessage("General.TimeLayoutS", "S", diff + "");
        }
        return duration;
    }

    public String getLayoutBSN() {
        StringBuilder msg = new StringBuilder();
        for (String str : getLayout()) msg.append("\n").append(str);
        return msg.substring(1);
    }

    public boolean isExpired() {
        return getType().isTemp() && getEnd() <= TimeManager.getTime();
    }

    @Override
    public String toString() {
        return "Punishment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                ", reason='" + reason + '\'' +
                ", operator='" + operator + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", calculation='" + calculation + '\'' +
                ", type=" + type +
                '}';
    }
}
