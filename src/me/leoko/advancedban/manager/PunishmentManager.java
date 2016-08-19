package me.leoko.advancedban.manager;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Leoko @ dev.skamps.eu on 30.05.2016.
 */
public class PunishmentManager {

    private static PunishmentManager instance = null;
    public static PunishmentManager get(){
        return instance == null ? instance = new PunishmentManager() : instance;
    }

    private List<Punishment> punishments = Collections.synchronizedList(new ArrayList<Punishment>());
    private List<Punishment> history = Collections.synchronizedList(new ArrayList<Punishment>());

    public void setup(){
        if(Universal.get().isUseMySQL()){
            try {
                if(!Universal.get().getMysql().getConnection().getMetaData().getTables(null, null, "Punishments", null).next()){
                    String sql = "CREATE TABLE `Punishments` ("  +
                            "`id` int NOT NULL AUTO_INCREMENT," +
                            "`name` TEXT NULL DEFAULT NULL,"+
                            "`uuid` TEXT NULL DEFAULT NULL," +
                            "`reason` TEXT NULL DEFAULT NULL,"+
                            "`operator` TEXT NULL DEFAULT NULL,"+
                            "`punishmentType` TEXT NULL DEFAULT NULL,"+
                            "`start` LONG DEFAULT NULL,"+
                            "`end` LONG DEFAULT NULL,"+
                            "`calculation` TEXT NULL DEFAULT NULL," +
                            "PRIMARY KEY (`id`))";
                    Universal.get().getMysql().executeSatement(sql);
                }

                if(!Universal.get().getMysql().getConnection().getMetaData().getTables(null, null, "PunishmentHistory", null).next()){
                    String sql = "CREATE TABLE `PunishmentHistory` ("  +
                            "`id` int NOT NULL AUTO_INCREMENT," +
                            "`name` TEXT NULL DEFAULT NULL,"+
                            "`uuid` TEXT NULL DEFAULT NULL," +
                            "`reason` TEXT NULL DEFAULT NULL,"+
                            "`operator` TEXT NULL DEFAULT NULL,"+
                            "`punishmentType` TEXT NULL DEFAULT NULL,"+
                            "`start` LONG DEFAULT NULL,"+
                            "`end` LONG DEFAULT NULL,"+
                            "`calculation` TEXT NULL DEFAULT NULL," +
                            "PRIMARY KEY (`id`))";
                    Universal.get().getMysql().executeSatement(sql);
                }

                Universal.get().getMysql().executeSatement("DELETE FROM `Punishments` WHERE `end` <= '"+TimeManager.getTime()+"' AND `end` != -1");
                ResultSet rs = Universal.get().getMysql().executeRespSatemen("SELECT * FROM `Punishments`");
                while(rs.next()) {
                    punishments.add(
                            new Punishment(rs.getString("name"),
                                    rs.getString("uuid"), rs.getString("reason"),
                                    rs.getString("operator"),
                                    PunishmentType.valueOf(rs.getString("punishmentType")),
                                    rs.getLong("start"),
                                    rs.getLong("end"),
                                    rs.getString("calculation"),
                                    rs.getInt("id")
                            )
                    );
                }

                ResultSet rsh = Universal.get().getMysql().executeRespSatemen("SELECT * FROM `PunishmentHistory`");
                while(rsh.next()){
                    history.add(
                            new Punishment(rsh.getString("name"),
                                    rsh.getString("uuid"), rsh.getString("reason"),
                                    rsh.getString("operator"),
                                    PunishmentType.valueOf(rsh.getString("punishmentType")),
                                    rsh.getLong("start"),
                                    rsh.getLong("end"),
                                    rsh.getString("calculation"),
                                    rsh.getInt("id")
                                    )
                            );
                }
            }catch (SQLException exc){
                exc.printStackTrace();
            }
        }else{
            MethodInterface mi = Universal.get().getMethods();
            if(mi.contains(mi.getData(), "Punishments")){
                for(String key : mi.getKeys(mi.getData(), "Punishments")) {
                    punishments.add(
                            new Punishment(mi.getString(mi.getData(), "Punishments."+key+".name"),
                                    mi.getString(mi.getData(), "Punishments."+key+".uuid"), mi.getString(mi.getData(), "Punishments."+key+".reason"),
                                    mi.getString(mi.getData(), "Punishments."+key+".operator"),
                                    PunishmentType.valueOf(mi.getString(mi.getData(), "Punishments."+key+".punishmentType")),
                                    mi.getLong(mi.getData(), "Punishments."+key+".start"),
                                    mi.getLong(mi.getData(), "Punishments."+key+".end"),
                                    mi.getString(mi.getData(), "Punishments."+key+"calculation"),
                                    Integer.valueOf(key)
                                    )
                            );
                }
            }
            if(mi.contains(mi.getData(), "PunishmentHistory")) {
                for(String key : mi.getKeys(mi.getData(), "PunishmentHistory")) {
                    history.add(
                            new Punishment(mi.getString(mi.getData(), "PunishmentHistory."+key+".name"),
                                    mi.getString(mi.getData(), "PunishmentHistory."+key+".uuid"), mi.getString(mi.getData(), "PunishmentHistory."+key+".reason"),
                                    mi.getString(mi.getData(), "PunishmentHistory."+key+".operator"),
                                    PunishmentType.valueOf(mi.getString(mi.getData(), "PunishmentHistory."+key+".punishmentType")),
                                    mi.getLong(mi.getData(), "PunishmentHistory."+key+".start"),
                                    mi.getLong(mi.getData(), "PunishmentHistory."+key+".end"),
                                    mi.getString(mi.getData(), "PunishmentHistory."+key+"calculation"),
                                    Integer.valueOf(key)
                            )
                            );
                }
            }
        }
    }

    public List<Punishment> getPunishments(String uuid, PunishmentType put, boolean current) {
        List<Punishment> punList = new ArrayList<Punishment>();
        for(Punishment pu : current ? punishments : history){
            if((put == null || put == pu.getType().getBasic()) && pu.getUuid().equals(uuid)){
                if(!current || !pu.isExpired()) punList.add(pu);
                else pu.delete();
            }
        }
        return punList;
    }

    public Punishment getWarn(int id){
        for(Punishment pt : getPunishments(true)){
            if(pt.getType().getBasic() == PunishmentType.WARNING && pt.getId() == id){
                return pt;
            }
        }
        return null;
    }

    public Punishment getBan(String uuid){
        for(Punishment pt : getPunishments(true)){
            if(pt.getType().getBasic() == PunishmentType.BAN && pt.getUuid().equals(uuid)){
                return pt;
            }
        }
        return null;
    }

    public Punishment getMute(String uuid){
        for(Punishment pt : getPunishments(true)){
            if(pt.getType().getBasic() == PunishmentType.MUTE && pt.getUuid().equals(uuid)){
                return pt;
            }
        }
        return null;
    }

    public boolean isBanned(String uuid){
        return getBan(uuid) != null;
    }

    public boolean isMuted(String uuid){
        return getMute(uuid) != null;
    }

    public int getCurrentWarns(String uuid){
        int i = 0;
        for(Punishment pt : getPunishments(true))
            if(pt.getType().getBasic() == PunishmentType.WARNING && pt.getUuid().equals(uuid))
                i++;
        return i;
    }

    public List<Punishment> getPunishments(boolean checkExpired) {
        if(checkExpired){
            List<Punishment> toDelete = new ArrayList<Punishment>();
            for(Punishment pu : punishments) if(pu.isExpired()) toDelete.add(pu);
            for(Punishment pu : toDelete) pu.delete();
        }
        return punishments;
    }

    public List<Punishment> getHistory() {
        return history;
    }
}
