package me.leoko.advancedban.utils.commands;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.MessageManager;
import me.leoko.advancedban.utils.Command;
import me.leoko.advancedban.utils.Punishment;

import java.util.function.Consumer;
import java.util.function.Function;

public class RevokeByIdProcessor implements Consumer<Command.CommandInput> {
    private String path;
    private Function<Integer, Punishment> resolver;

    public RevokeByIdProcessor(String path, Function<Integer, Punishment> resolver) {
        this.path = path;
        this.resolver = resolver;
    }


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
