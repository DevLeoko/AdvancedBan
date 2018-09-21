package me.leoko.advancedban.punishment;

import lombok.RequiredArgsConstructor;
import me.leoko.advancedban.manager.PunishmentManager;

import java.net.InetAddress;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Leo on 04.08.2017.
 */
@RequiredArgsConstructor
public class InterimData {

    private final UUID uuid;
    private final String name;
    private final InetAddress address;
    private final Set<Punishment> punishments;
    private final Set<Punishment> history;

    public Optional<Punishment> getBan() {
        for (Punishment pt : punishments) {
            if (pt.getType().getBasic() == PunishmentType.BAN && !pt.isExpired()) {
                return Optional.of(pt);
            }
        }
        return Optional.empty();
    }

    public void accept(PunishmentManager manager) {
        manager.getLoadedPunishments(false).addAll(punishments);
        manager.getLoadedHistory().addAll(history);
        manager.addCached(name);
        manager.addCached(address);
        manager.addCached(uuid);
    }
}