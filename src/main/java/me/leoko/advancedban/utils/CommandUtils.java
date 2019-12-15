package me.leoko.advancedban.utils;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.MessageManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;

public class CommandUtils {
    public static Punishment getPunishment(String target, PunishmentType type) {
        return type == PunishmentType.MUTE
                ? PunishmentManager.get().getMute(target)
                : PunishmentManager.get().getBan(target);
    }

    // Removes name argument and returns uuid (null if failed)
    public static String processName(Command.CommandInput input) {
        String name = input.getPrimary();
        input.next();
        String uuid = UUIDManager.get().getUUID(name.toLowerCase());

        if (uuid == null)
            MessageManager.sendMessage(input.getSender(), "General.FailedFetch",
                    true, "NAME", name);

        return uuid;
    }

    // Removes name/ip argument and returns ip (null if failed)
    public static String processIP(Command.CommandInput input) {
        String name = input.getPrimaryData();
        input.next();
        if (name.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
            return name;
        }
		String ip = Universal.get().getIps().get(name);

		if (ip == null)
		    MessageManager.sendMessage(input.getSender(), "Ipban.IpNotCashed",
		            true, "NAME", name);

		return ip;
    }

    // Builds reason from remaining arguments (null if failed)
    public static String processReason(Command.CommandInput input) {
        MethodInterface mi = Universal.get().getMethods();
        String reason = String.join(" ", input.getArgs());

        if (reason.matches("[~@].+") && !mi.contains(mi.getLayouts(), "Message." + reason.substring(1))) {
            MessageManager.sendMessage(input.getSender(), "General.LayoutNotFound",
                    true, "NAME", reason.substring(1));
            return null;
        }

        return reason;
    }
}
