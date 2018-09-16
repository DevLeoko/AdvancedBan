package me.leoko.advancedban.bungee;

import me.leoko.advancedban.AdvancedBanCommandSender;
import me.leoko.advancedban.command.AbstractCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;


public class BungeeAdvancedBanCommand extends Command {
    private final AbstractCommand command;
    private final BungeeAdvancedBan advancedBan;

    public BungeeAdvancedBanCommand(AbstractCommand command, BungeeAdvancedBan advancedBan) {
        super(command.getName(), null, command.getAliases());
        this.command = command;
        this.advancedBan = advancedBan;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        AdvancedBanCommandSender advancedBanCommandSender;
        if (sender instanceof ProxiedPlayer) {
            advancedBanCommandSender = advancedBan.getPlayer(((ProxiedPlayer) sender).getUniqueId())
                    .orElseThrow(() -> new IllegalStateException("Player is not registered within AdvancedBan"));
        } else {
            advancedBanCommandSender = new BungeeAdvancedBanCommandSender(sender, advancedBan, advancedBan.getProxy());
        }

        command.execute(advancedBanCommandSender, args);
    }
}
