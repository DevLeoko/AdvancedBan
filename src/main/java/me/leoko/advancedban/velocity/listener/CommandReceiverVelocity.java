package me.leoko.advancedban.velocity.listener;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.CommandManager;
import me.leoko.advancedban.velocity.VelocityMain;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class CommandReceiver implements Command {
    private final String cmd;
    public CommandReceiver(String cmd) {
        this.cmd = cmd;
    }

    @Override
    public void execute(CommandSource commandSource, @NonNull String[] args) {
        if (args.length > 0) {
            args[0] = (VelocityMain.get().server.getPlayer(args[0]).isPresent() ? VelocityMain.get().server.getPlayer(args[0]).get().getUsername() : args[0]);
        }
        CommandManager.get().onCommand(commandSource, cmd, args);
    }

    @Override
    public List<String> suggest(CommandSource source, @NonNull String[] args) {
        final me.leoko.advancedban.utils.Command command = me.leoko.advancedban.utils.Command.getByName(cmd.substring(1));
        if (command != null && source instanceof Player) {
            if (command.getPermission() == null || Universal.get().getMethods().hasPerms(source, command.getPermission())) {
                return command.getTabCompleter().onTabComplete(source, args);
            }
        }
        return new ArrayList<>();
    }
}
