package me.leoko.advancedban.bukkit.listener;

import me.leoko.advancedban.bukkit.event.PunishmentEvent;
import me.leoko.advancedban.bukkit.event.RevokePunishmentEvent;
import me.leoko.advancedban.utils.PunishmentType;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Date;

/**
 *
 * @author Beelzebu
 */
public class InternalListener implements Listener {

    private boolean canSafelyBan(String playerName) {
        boolean userKnown = false;
        if (playerName != null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            userKnown = offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline();
        }

        if (!userKnown) {
            Bukkit.getLogger().warning("Cannot ban " + playerName + ": player has never joined and GameProfile may be null");
        }

        return userKnown;
    }

    private void ban(BanList banlist, PunishmentEvent e) {
        String playerName = e.getPunishment().getName();
        if ( !canSafelyBan(playerName)) {
            return;
        }

        banlist.addBan(playerName, e.getPunishment().getReason(), new Date(e.getPunishment().getEnd()), e.getPunishment().getOperator());
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
        String playerName = e.getPunishment().getName();
        if ( !canSafelyBan(playerName)) {
            return;
        }

        banlist.pardon(playerName);
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