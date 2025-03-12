package me.leoko.advancedban.bukkit.listener;

import me.leoko.advancedban.bukkit.event.PunishmentEvent;
import me.leoko.advancedban.bukkit.event.RevokePunishmentEvent;
import me.leoko.advancedban.utils.PunishmentType;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Date;

/**
 *
 * @author Beelzebu
 */
public class InternalListener implements Listener {

    private void ban(BanList banlist, PunishmentEvent e) {
        try {
            banlist.addBan(e.getPunishment().getName(), e.getPunishment().getReason(), new Date(e.getPunishment().getEnd()), e.getPunishment().getOperator());
        } catch (NullPointerException ex) {
            Bukkit.getLogger().severe("No player is known by the name '" + e.getPunishment().getName() + "'");
        }
    }

    @EventHandler
    public void onPunish(PunishmentEvent e) {
        BanList banlist;
        if (e.getPunishment().getType().equals(PunishmentType.BAN) || e.getPunishment().getType().equals(PunishmentType.TEMP_BAN)) {
            banlist = Bukkit.getBanList(BanList.Type.NAME);
            ban(banlist, e);
        } else if (e.getPunishment().getType().equals(PunishmentType.IP_BAN) || e.getPunishment().getType().equals(PunishmentType.TEMP_IP_BAN)) {
            banlist = Bukkit.getBanList(BanList.Type.IP);
            ban(banlist, e);
        }
    }

    private void pardon(BanList banlist, RevokePunishmentEvent e) {
        try {
            banlist.pardon(e.getPunishment().getName());
        } catch (NullPointerException ex) {
            Bukkit.getLogger().severe("No player is known by the name '" + e.getPunishment().getName() + "'");
        }
    }

    @EventHandler
    public void onRevokePunishment(RevokePunishmentEvent e) {
        BanList banlist;
        if (e.getPunishment().getType().equals(PunishmentType.BAN) || e.getPunishment().getType().equals(PunishmentType.TEMP_BAN)) {
            banlist = Bukkit.getBanList(BanList.Type.NAME);
            pardon(banlist, e);
        } else if (e.getPunishment().getType().equals(PunishmentType.IP_BAN) || e.getPunishment().getType().equals(PunishmentType.TEMP_IP_BAN)) {
            banlist = Bukkit.getBanList(BanList.Type.IP);
            pardon(banlist, e);
        }
    }
}