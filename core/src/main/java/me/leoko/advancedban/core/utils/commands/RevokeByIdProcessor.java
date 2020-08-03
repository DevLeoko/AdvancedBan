package me.leoko.advancedban.core.utils.commands;

import lombok.AllArgsConstructor;
import me.leoko.advancedban.core.Universal;
import me.leoko.advancedban.core.manager.MessageManager;
import me.leoko.advancedban.core.utils.Command;
import me.leoko.advancedban.core.utils.Punishment;

import java.util.function.Consumer;
import java.util.function.Function;

@AllArgsConstructor
public class RevokeByIdProcessor implements Consumer<Command.CommandInput> {
    private String path;
    private Function<Integer, Punishment> resolver;


    @Override
    public void accept(Command.CommandInput input) {
        int id = Integer.parseInt(input.getPrimary());

        Punishment punishment = resolver.apply(id);
        if (punishment == null) {
            MessageManager.sendMessage(input.getSender(), path + ".NotFound",
                    true, "ID", id + "");
            return;
        }

        final String operator = Universal.get().getMethods().getName(input.getSender());
        punishment.delete(operator, false, true);
        MessageManager.sendMessage(input.getSender(), path + ".Done",
                true, "ID", id + "");
    }
}
