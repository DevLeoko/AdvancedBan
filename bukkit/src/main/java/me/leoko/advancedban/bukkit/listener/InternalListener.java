package me.leoko.advancedban.bukkit.listener;

import me.leoko.advancedban.bukkit.event.PunishmentEvent;
import me.leoko.advancedban.bukkit.event.RevokePunishmentEvent;
import me.leoko.advancedban.manager.DatabaseManager;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Date;

/**
 * Mirrors bans into the vanilla ban lists.
 * This uses the deprecated name based API as the replacement does not exist on older servers.
 *
 * @author Beelzebu
 */
@SuppressWarnings("deprecation")
public class InternalListener implements Listener {
    
    @EventHandler
    public void onPunish(PunishmentEvent e) {
        Punishment punishment = e.getPunishment();
        // Vanilla treats a null date as permanent, -1 would be a ban which expired in 1969
        Date end = punishment.getType().isTemp() ? new Date(punishment.getEnd()) : null;
        // With a shared database the unban can happen on another server, this entry would then lock out the player forever
        if (end == null && DatabaseManager.get().isUseMySQL()) {
            return;
        }
        if (isIpBan(punishment)) {
            Bukkit.getBanList(BanList.Type.IP).addBan(punishment.getUuid(), punishment.getReason(), end, punishment.getOperator());
        } else if (isNameBan(punishment)) {
            Bukkit.getBanList(BanList.Type.NAME).addBan(punishment.getName(), punishment.getReason(), end, punishment.getOperator());
        }
    }
    
    @EventHandler
    public void onRevokePunishment(RevokePunishmentEvent e) {
        Punishment punishment = e.getPunishment();
        if (isIpBan(punishment)) {
            Bukkit.getBanList(BanList.Type.IP).pardon(punishment.getUuid());
        } else if (isNameBan(punishment)) {
            Bukkit.getBanList(BanList.Type.NAME).pardon(punishment.getName());
        }
    }

    // The target of an ip ban is stored as its uuid, the name is just the input of the operator
    private boolean isIpBan(Punishment punishment) {
        return punishment.getType().isIpOrientated();
    }

    private boolean isNameBan(Punishment punishment) {
        return punishment.getType().getBasic() == PunishmentType.BAN && !punishment.getType().isIpOrientated();
    }
}