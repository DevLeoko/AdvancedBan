package me.leoko.advancedban.bukkit;

import lombok.RequiredArgsConstructor;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanCommandSender;
import me.leoko.advancedban.command.AbstractCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class AdvancedBanCommandExecutor implements CommandExecutor {
    private final AbstractCommand command;
    private final AdvancedBan advancedBan;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        AdvancedBanCommandSender advancedBanCommandSender;
        if (sender instanceof Player) {
            advancedBanCommandSender = advancedBan.getPlayer(((Player) sender).getUniqueId())
                    .orElseThrow(() -> new IllegalStateException("Player is not registered within AdvancedBan"));
        } else {
            advancedBanCommandSender = new BukkitAdvancedBanCommandSender(sender, advancedBan);
        }

        this.command.execute(advancedBanCommandSender, args);
        return true;
    }
}
