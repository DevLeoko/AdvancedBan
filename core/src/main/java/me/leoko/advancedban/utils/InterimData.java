package me.leoko.advancedban.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.leoko.advancedban.manager.PunishmentManager;

import java.util.Set;

/**
 * Created by Leo on 04.08.2017.
 */
@Getter
@AllArgsConstructor
public class InterimData {

    private final String uuid, name, ip;
    private final Set<Punishment> punishments, history;

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