package me.leoko.advancedban.bungee.listener;

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

    @Override
    public void execute(CommandSender sender, String[] args) {
        CommandManager.get().onCommand(sender, getName(), args);
    }
}
