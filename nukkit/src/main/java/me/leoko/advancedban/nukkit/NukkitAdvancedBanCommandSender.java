package me.leoko.advancedban.nukkit;

import cn.nukkit.command.CommandSender;
import lombok.RequiredArgsConstructor;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanCommandSender;

@RequiredArgsConstructor
public class NukkitAdvancedBanCommandSender implements AdvancedBanCommandSender {
    private final CommandSender sender;
    private final AdvancedBan advancedBan;

    @Override
    public AdvancedBan getAdvancedBan() {
        return advancedBan;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(message);
    }

    @Override
    public boolean executeCommand(String command) {
        return sender.getServer().dispatchCommand(sender, command);
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }
}
