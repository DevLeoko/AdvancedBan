package me.leoko.advancedban.manager;

import lombok.RequiredArgsConstructor;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.command.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class CommandManager {
    private final Map<String, AbstractCommand> registeredCommands = new HashMap<>();
    private final AdvancedBan advancedBan;

    public void onEnable() {
        registerCommand(new AdvancedBanCommand());
        registerCommand(new BanCommand());
        registerCommand(new BanlistCommand());
        registerCommand(new ChangeReasonCommand());
        registerCommand(new CheckCommand());
        registerCommand(new HistoryCommand());
        registerCommand(new IPBanCommand());
        registerCommand(new KickCommand());
        registerCommand(new MuteCommand());
        registerCommand(new SystemPreferencesCommand());
        registerCommand(new TempBanCommand());
        registerCommand(new TempIPBanCommand());
        registerCommand(new TempMuteCommand());
        registerCommand(new TempWarningCommand());
        registerCommand(new UnbanCommand());
        registerCommand(new UnmuteCommand());
        registerCommand(new UnpunishCommand());
        registerCommand(new UnwarnCommand());
        registerCommand(new WarningCommand());
        registerCommand(new WarnsCommand());
    }

    private void registerCommand(AbstractCommand command) {
        registeredCommands.put(command.getName(), command);
        advancedBan.registerCommand(command);
    }

    public Optional<AbstractCommand> getCommand(String command) {
        return Optional.ofNullable(registeredCommands.get(command));
    }
}
