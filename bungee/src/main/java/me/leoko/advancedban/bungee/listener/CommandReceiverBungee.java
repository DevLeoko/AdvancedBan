package me.leoko.advancedban.bungee.listener;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.bungee.BungeeMain;
import me.leoko.advancedban.manager.CommandManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

/**
 * Created by Leoko @ dev.skamps.eu on 24.07.2016.
 */

public class CommandReceiverBungee extends Command {

    /**
     * @param name       name of the command
     * @param permission permission required to use the command. May be null
     */
    public CommandReceiverBungee(String name, String permission) {
        super(name, permission);
    }
    
    /**
     * BungeeCord checks this before executing the command, so it has to be aware of the ab.all permission nodes as well.
     */
    @Override
    public boolean hasPermission(CommandSender sender) {
        return getPermission() == null || Universal.get().hasPerms(sender, getPermission());
    }

    @Override
	public void execute(final CommandSender sender, final String[] args) {
    	if (args.length > 0) {
    		args[0] = (BungeeMain.get().getProxy().getPlayer(args[0]) != null ? BungeeMain.get().getProxy().getPlayer(args[0]).getName() : args[0]);
    	}
        CommandManager.get().onCommand(sender, this.getName(), args);
    }
    
}
