package me.leoko.advancedban.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.CommandManager;
import java.util.Collections;
import java.util.List;

public class CommandRecieverVelocity implements SimpleCommand {

  private final ProxyServer server;
  private final String cmd;

  public CommandRecieverVelocity(ProxyServer server, String cmd) {
    this.cmd = cmd;
    this.server = server;
  }

  @Override
  public void execute(Invocation invocation) {
    String[] args = invocation.arguments();
    CommandSource commandSource = invocation.source();
    if (invocation.arguments().length > 0) {
      args[0] = (this.server.getPlayer(args[0]).isPresent() ? this.server.getPlayer(args[0]).get().getUsername() : args[0]);
    }
    CommandManager.get().onCommand(commandSource, cmd, args);
  }

  @Override
  public List<String> suggest(Invocation invocation) {
    String[] args = invocation.arguments();
    CommandSource source = invocation.source();
    final me.leoko.advancedban.utils.Command command = me.leoko.advancedban.utils.Command.getByName(cmd.substring(1));
      if (command.getPermission() == null || Universal.get().getMethods().hasPerms(source, command.getPermission())) {
        return command.getTabCompleter().onTabComplete(source, args);
      }
    return Collections.emptyList();
  }
}
