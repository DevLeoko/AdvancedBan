package me.leoko.advancedban.utils.commands;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.AltManager;
import me.leoko.advancedban.manager.MessageManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.utils.Command;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static me.leoko.advancedban.utils.CommandUtils.processName;

public class AltsProcessor implements Consumer<Command.CommandInput> {

    @Override
    public void accept(Command.CommandInput input) {
        AltManager altManager = AltManager.get();
        Object sender = input.getSender();

        if (!altManager.isEnabled()) {
            MessageManager.sendMessage(sender, "Alts.Disabled", true);
            return;
        }

        String name = input.getPrimary();
        List<AltManager.Account> accounts;
        if (name.contains(":") || name.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
            accounts = altManager.getAccounts(name);
        } else {
            String uuid = processName(input);
            if (uuid == null) {
                return;
            }
            accounts = altManager.getAlts(uuid);
        }

        if (accounts.isEmpty()) {
            MessageManager.sendMessage(sender, "Alts.NoEntries", true, "NAME", name);
            return;
        }

        MethodInterface mi = Universal.get().getMethods();
        SimpleDateFormat format = new SimpleDateFormat(mi.getString(mi.getConfig(), "DateFormat", "dd.MM.yyyy-HH:mm"));

        MessageManager.sendMessage(sender, "Alts.Header", true, "NAME", name);
        for (AltManager.Account account : accounts) {
            MessageManager.sendMessage(sender, "Alts.Entry", false,
                    "ALT", account.getName(),
                    "STATUS", MessageManager.getMessage(status(account.getUuid())),
                    "DATE", format.format(new Date(account.getLastSeen())));
        }
    }

    private static String status(String uuid) {
        if (PunishmentManager.get().isBanned(uuid)) {
            return "Alts.Banned";
        }
        return PunishmentManager.get().isMuted(uuid) ? "Alts.Muted" : "Alts.Clean";
    }
}
