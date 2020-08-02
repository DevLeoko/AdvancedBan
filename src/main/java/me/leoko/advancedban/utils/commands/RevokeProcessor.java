package me.leoko.advancedban.utils.commands;

import lombok.AllArgsConstructor;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.MessageManager;
import me.leoko.advancedban.utils.Command;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;

import java.util.function.Consumer;

import static me.leoko.advancedban.utils.CommandUtils.*;

@AllArgsConstructor
public class RevokeProcessor implements Consumer<Command.CommandInput> {
    private PunishmentType type;

    @Override
    public void accept(Command.CommandInput input) {
        String name = input.getPrimary();

        String target = name;
        if(!target.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
            target = processName(input);
            if (target == null)
                return;
        }

        Punishment punishment = getPunishment(target, type);
        if (punishment == null) {
            MessageManager.sendMessage(input.getSender(), "Un" + type.getName() + ".NotPunished",
                    true, "NAME", name);
            return;
        }

        final String operator = Universal.get().getMethods().getName(input.getSender());
        punishment.delete(operator, false, true);
        MessageManager.sendMessage(input.getSender(), "Un" + type.getName() + ".Done",
                true, "NAME", name);
    }
}
