package me.leoko.advancedban.nukkit;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanCommandSender;
import me.leoko.advancedban.command.AbstractCommand;

public class NukkitAdvancedBanCommand extends Command {
    private final AbstractCommand command;
    private final AdvancedBan advancedBan;

    public NukkitAdvancedBanCommand(AbstractCommand command, AdvancedBan advancedBan) {
        super(command.getName());
        this.command = command;
        this.advancedBan = advancedBan;
    }

    @Override
    public boolean execute(CommandSender sender, String cmd, String[] args) {
        AdvancedBanCommandSender advancedBanCommandSender;
        if (sender instanceof Player) {
            advancedBanCommandSender = advancedBan.getPlayer(((Player) sender).getUniqueId())
                    .orElseThrow(() -> new IllegalStateException("Player is not registered within AdvancedBan"));
        } else {
            advancedBanCommandSender = new NukkitAdvancedBanCommandSender(sender, advancedBan);
        }

        this.command.execute(advancedBanCommandSender, args);
        return true;
    }
}
