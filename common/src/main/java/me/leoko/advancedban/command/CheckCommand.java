package me.leoko.advancedban.command;

import me.leoko.advancedban.AdvancedBanCommandSender;
import me.leoko.advancedban.punishment.Punishment;
import me.leoko.advancedban.utils.CommandUtils;
import me.leoko.advancedban.utils.GeoLocation;

import java.net.InetAddress;
import java.util.Optional;
import java.util.UUID;

public class CheckCommand extends AbstractCommand {

    public CheckCommand() {
        super("check", "ab.check", "Check");
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static String getPunishmentMapping(Optional<Punishment> punishment) {
        return punishment.map(pun -> pun.getType().isTemp() ? "\u00A7e" + pun.getDuration(false) : "\u00A7cPermanent")
                .orElse("\u00A72None");
    }

    private static Optional<Punishment> getPunishment(AdvancedBanCommandSender sender, String id) {
        try {
            return sender.getAdvancedBan().getPunishmentManager().getPunishment(Integer.parseInt(id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean onCommand(AdvancedBanCommandSender sender, String[] args) {
        if (args.length != 1) {
            return false;
        }

        Object identifier;
        Optional<Punishment> punishment = getPunishment(sender, args[0]);

        if (punishment.isPresent()) {
            identifier = punishment.get().getIdentifier();
        } else {
            Optional other = CommandUtils.getIdentifier(sender.getAdvancedBan(), args[0]);
            if (!other.isPresent()) {
                sender.sendCustomMessage("General.FailedFetch", true, "NAME", args[0]);
                return false;
            }
            identifier = other.get();
        }

        if (identifier instanceof InetAddress && !sender.hasPermission("ab.check.ip")) {
            sendPermissionMessage(sender);
            return true;
        }

        Optional<InetAddress> address = sender.getAdvancedBan().getAddress(identifier);
        Optional<String> location = Optional.empty();
        if (address.isPresent()) {
            location = GeoLocation.getLocation(address.get());
        }
        Optional<Punishment> mute = sender.getAdvancedBan().getPunishmentManager().getMute(identifier);
        Optional<Punishment> ban = sender.getAdvancedBan().getPunishmentManager().getBan(identifier);

        sender.sendCustomMessage("Check.Header", true, "NAME", punishment.map(Punishment::getName).orElse(args[0]));
        sender.sendCustomMessage("Check.UUID", false, "UUID", identifier instanceof UUID ? identifier : "N/A");
        if (sender.hasPermission("ab.check.ip")) {
            sender.sendCustomMessage("Check.IP", false, "IP", address.map(InetAddress::getHostAddress).orElse("N/A"));
        }
        sender.sendCustomMessage("Check.Geo", false, "LOCATION", location.orElse("N/A"));
        sender.sendCustomMessage("Check.Mute", false, "DURATION", getPunishmentMapping(mute));
        mute.ifPresent(presentMute -> sender.sendCustomMessage("Check.MuteReason", false, "REASON", presentMute.getReason()));
        sender.sendCustomMessage("Check.Ban", false, "DURATION", getPunishmentMapping(mute));
        ban.ifPresent(presentBan -> sender.sendCustomMessage("Check.BanReason", false, "REASON", presentBan.getReason()));
        sender.sendCustomMessage("Check.Warn", false, "COUNT", sender.getAdvancedBan().getPunishmentManager().getCurrentWarns(identifier));
        return true;
    }
}
