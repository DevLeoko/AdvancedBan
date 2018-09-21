package me.leoko.advancedban.command;

import me.leoko.advancedban.AdvancedBanCommandSender;
import me.leoko.advancedban.AdvancedBanPlayer;
import me.leoko.advancedban.punishment.PunishmentType;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class WarnsCommand extends AbstractCommand {

    public WarnsCommand() {
        super("warns", null, "Warns", "warnings");
    }

    @Override
    public boolean onCommand(AdvancedBanCommandSender sender, String[] args) {
        int page = 1;
        if (args.length > 0 && args[args.length - 1].toLowerCase().matches("[1-9][0-9]*")) {
            page = Integer.parseInt(args[args.length - 1].toLowerCase());
            args = Arrays.copyOf(args, args.length - 1);
        }
        UUID uuid;
        String name;
        if (args.length == 0 && sender instanceof AdvancedBanPlayer) {
            if (!sender.hasPermission("ab.warns.own")) {
                sendPermissionMessage(sender);
                return true;
            }
            uuid = ((AdvancedBanPlayer) sender).getUniqueId();
            name = sender.getName();
        } else if (args.length == 1) {
            if (!sender.hasPermission("ab.warns.own")) {
                sendPermissionMessage(sender);
                return true;
            }
            Optional<UUID> other = sender.getAdvancedBan().getUuidManager().getUUID(args[0]);
            if (!other.isPresent()) {
                sender.sendCustomMessage("General.FailedFetch", true, "NAME", args[0]);
                return true;
            }
            name = args[0];
            uuid = other.get();
        } else {
            return false;
        }
        performList(
                sender,
                page,
                sender.getAdvancedBan().getPunishmentManager().getPunishments(uuid, PunishmentType.WARNING, true),
                name,
                false
        );
        return true;
    }
}
