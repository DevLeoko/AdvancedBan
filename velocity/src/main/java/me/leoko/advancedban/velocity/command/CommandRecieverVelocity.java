package me.leoko.advancedban.velocity.command;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.CommandManager;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.ArrayList;
import java.util.List;

public class CommandRecieverVelocity implements SimpleCommand {

  @Inject
  private ProxyServer server;

  @MonotonicNonNull private final String cmd;
  @MonotonicNonNull private String[] args;
  @MonotonicNonNull private CommandSource commandSource;

  public CommandRecieverVelocity(String cmd) {
    this.cmd = cmd;
  }

  @Override
  public void execute(Invocation invocation) {
    this.args = invocation.arguments();
    this.commandSource = invocation.source();
    if (invocation.arguments().length > 0) {
      args[0] = (this.server.getPlayer(args[0]).isPresent() ? this.server.getPlayer(args[0]).get().getUsername() : args[0]);
    }
    CommandManager.get().onCommand(commandSource, cmd, args);
  }

  @Override
  public List<String> suggest(Invocation invocation) {
    final me.leoko.advancedban.utils.Command command = me.leoko.advancedban.utils.Command.getByName(cmd.substring(1));
    if (command != null && invocation.source() instanceof Player) {
      if (command.getPermission() == null || Universal.get().getMethods().hasPerms(commandSource, command.getPermission())) {
        return command.getTabCompleter().onTabComplete(commandSource, args);
      }
    }
    return new ArrayList<>();
  }
}
