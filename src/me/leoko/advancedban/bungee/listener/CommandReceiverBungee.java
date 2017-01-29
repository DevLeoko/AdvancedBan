package me.leoko.advancedban.bungee.listener;

import me.leoko.advancedban.bungee.BungeeMain;
import me.leoko.advancedban.manager.CommandManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

/**
 * Created by Leoko @ dev.skamps.eu on 24.07.2016.
 */
public class CommandReceiverBungee extends Command implements TabExecutor{

    public CommandReceiverBungee(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        CommandManager.get().onCommand(sender, getName(), args);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] strings) {
        Set<String> matches = new HashSet<>();
+        if ( args.length == 1 )
+        {
+            String search = args[0].toLowerCase();
+            for ( String player : BungeeMain.getOnlinePlayers() )
+            {
+                if ( player.toLowerCase().startsWith( search ) )
+                {
+                    matches.add( player );
+                }
+            }
+        }
        return matches;
    }
}
