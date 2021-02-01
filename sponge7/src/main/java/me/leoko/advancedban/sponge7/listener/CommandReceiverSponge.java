package me.leoko.advancedban.sponge7.listener;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.CommandManager;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CommandReceiverSponge implements CommandCallable {

    private final Game game;
    private final String cmd;

    public CommandReceiverSponge(Game game, String cmd) {
        this.cmd = cmd;
        this.game = game;
    }

    @Override
    public CommandResult process(CommandSource source, String arguments) {
        String[] splitArg = arguments.split(" ");
        if (splitArg.length > 0) {
            splitArg[0] = (this.game.getServer().getPlayer(splitArg[0]).isPresent() ? this.game.getServer().getPlayer(splitArg[0]).get().getName() : splitArg[0]);
        }
        CommandManager.get().onCommand(source, cmd, splitArg);
        return CommandResult.success();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) {
        final me.leoko.advancedban.utils.Command command = me.leoko.advancedban.utils.Command.getByName(cmd);
        if (command.getPermission() == null || Universal.get().getMethods().hasPerms(source, command.getPermission())) {
            return command.getTabCompleter().onTabComplete(source, arguments.split(" "));
        }
        return Collections.emptyList();
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return true;
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.empty();
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return Optional.empty();
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of("");
    }
}
