package me.leoko.advancedban.command;

import me.leoko.advancedban.AdvancedBanCommandSender;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

public class SystemPreferencesCommand extends AbstractCommand {

    public SystemPreferencesCommand() {
        super("systempreferences", "ab.check", null, "sp", "systemprefs");
    }

    @Override
    public boolean onCommand(AdvancedBanCommandSender sender, String[] args) {
        Calendar calendar = new GregorianCalendar();
        sender.sendMessage("§c§lAdvancedBan §cSystem Preferences");
        sender.sendMessage("§cServer-Time §8» §7" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
        if (args.length == 1) {
            String name = args[0];
            sender.sendMessage("§c" + args[0] + "'s UUID (Internal) §8» §7" + sender.getAdvancedBan().getPlayer(name).map(p -> p.getUniqueId().toString()).orElse("N/A"));
            sender.sendMessage("§c" + args[0] + "'s UUID (Fetched) §8» §7" + sender.getAdvancedBan().getUuidManager().getUUID(name).map(UUID::toString).orElse("N/A"));
        }
        return true;
    }
}
