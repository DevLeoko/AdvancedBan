package me.leoko.advancedban.listener;

import me.leoko.advancedban.manager.CommandManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by Leoko @ dev.skamps.eu on 23.07.2016.
 */
public class CommandReceiver implements CommandExecutor {

    private static CommandReceiver instance = null;

    public static CommandReceiver get() {
        return instance == null ? instance = new CommandReceiver() : instance;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        CommandManager.get().onCommand(commandSender, command.getName(), strings);
        return true;
    }
}
