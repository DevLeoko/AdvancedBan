package me.leoko.advancedban.bungee.listener;

import me.leoko.advancedban.bungee.BungeeMain;
import me.leoko.advancedban.manager.CommandManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

/**
 * Created by Leoko @ dev.skamps.eu on 24.07.2016.
 */
public class CommandReceiverBungee extends Command {

    public CommandReceiverBungee(String name) {
        super(name);
    }
    
    public void execute(final CommandSender sender, final String[] args) {
    	args[0] = (BungeeMain.get().getProxy().getPlayer(args[0]) != null ? BungeeMain.get().getProxy().getPlayer(args[0]).getName() : args[0]);
        CommandManager.get().onCommand(sender, this.getName(), args);
    }
}
