package me.leoko.advancedban.utils;

import lombok.experimental.UtilityClass;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanCommandSender;
import me.leoko.advancedban.AdvancedBanPlayer;
import me.leoko.advancedban.punishment.PunishmentType;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.regex.Pattern;

@UtilityClass
public class CommandUtils {

    private static final Pattern IP_ADDRESS_PATTERN = Pattern.compile("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");

    public static OptionalInt parseInt(String integer) {
        try {
            return OptionalInt.of(Integer.parseInt(integer));
        } catch (Exception e) {
            return OptionalInt.empty();
        }
    }

    public static Object getIdentifier(AdvancedBanCommandSender sender, PunishmentType type, String stringIdentifier) {
        type = type.getBasic();
        Object identifier;
        if (type != PunishmentType.IP_BAN && type != PunishmentType.TEMP_IP_BAN) {
            Optional<UUID> uuid = sender.getAdvancedBan().getUuidManager().getUuid(stringIdentifier);
            if (!uuid.isPresent()) {
                sender.sendCustomMessage("General.FailedFetch", true, "NAME", stringIdentifier);
                return true;
            }
            identifier = uuid.get();
        } else {
            if (stringIdentifier.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
                try {
                    identifier = InetAddress.getByName(stringIdentifier);
                } catch (UnknownHostException e) {
                    sender.sendCustomMessage("General.FailedFetch", true, "NAME", stringIdentifier);
                    return true;
                }
            } else {
                Optional<AdvancedBanPlayer> player = sender.getAdvancedBan().getPlayer(stringIdentifier);
                if (player.isPresent()) {
                    identifier = player.get().getAddress().getAddress();
                } else {
                    sender.sendCustomMessage("Ipban.IpNotCashed", true, "NAME", stringIdentifier);
                    return true;
                }
            }
        }
        return identifier;
    }

    public static Optional getIdentifier(AdvancedBan advancedBan, String arg) {
        if (IP_ADDRESS_PATTERN.matcher(arg).matches()) {
            try {
                return Optional.of(InetAddress.getByName(arg));
            } catch (UnknownHostException e) {
                return Optional.empty();
            }
        } else {
            Optional identifier = advancedBan.getUuidManager().getUuid(arg);
            if (!identifier.isPresent()) {
                try {
                    identifier = Optional.of(UUID.fromString(arg));
                } catch (IllegalArgumentException e) {
                    // Ignore
                }
            }
            return identifier;
        }
    }
}
