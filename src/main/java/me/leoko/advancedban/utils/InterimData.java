package me.leoko.advancedban.utils;

import me.leoko.advancedban.manager.PunishmentManager;

import java.util.List;

/**
 * Created by Leo on 04.08.2017.
 */
public class InterimData {
    private String uuid, name, ip;
    private List<Punishment> punishments, history;

    public InterimData(String uuid, String name, String ip, List<Punishment> punishments, List<Punishment> history) {
        this.uuid = uuid;
        this.name = name;
        this.ip = ip;
        this.punishments = punishments;
        this.history = history;
    }

    public Punishment getBan() {
        for (Punishment pt : punishments) {
            if (pt.getType().getBasic() == PunishmentType.BAN && !pt.isExpired()) {
                return pt;
            }
        }
        return null;
    }

    public void accept(){
        PunishmentManager.get().getLoadedPunishments(false).addAll(punishments);
        PunishmentManager.get().getLoadedHistory().addAll(history);
        PunishmentManager.get().addCached(name);
        PunishmentManager.get().addCached(ip);
        PunishmentManager.get().addCached(uuid);
    }
}
