package me.leoko.advancedban.utils;

import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.utils.punishment.Identifier;
import me.leoko.advancedban.utils.punishment.Punishment;
import me.leoko.advancedban.utils.punishment.PunishmentType;

import java.util.Set;

/**
 * Created by Leo on 04.08.2017.
 */
public class InterimData {

    private final String  name;
    private final Identifier uuid, ip;
    private final Set<Punishment> punishments, history;

    public InterimData(String name, Identifier uuid, Identifier ip, Set<Punishment> punishments, Set<Punishment> history) {
        this.uuid = uuid;
        this.name = name;
        this.ip = ip;
        this.punishments = punishments;
        this.history = history;
    }

    public Identifier getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Identifier getIp() {
        return ip;
    }

    public Set<Punishment> getPunishments() {
        return punishments;
    }

    public Set<Punishment> getHistory() {
        return history;
    }

    public Punishment getBan() {
        for (Punishment pt : punishments) {
            if (pt.getType().getBasic() == PunishmentType.BAN && !pt.isExpired()) {
                return pt;
            }
        }
        return null;
    }

    public void accept() {
        PunishmentManager.get().getLoadedPunishments(false).addAll(punishments);
        PunishmentManager.get().getLoadedHistory().addAll(history);
        PunishmentManager.get().setCached(this);
    }
}