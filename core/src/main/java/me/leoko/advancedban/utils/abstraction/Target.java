package me.leoko.advancedban.utils.abstraction;

import me.leoko.advancedban.utils.commands.CommandExecutor;
import me.leoko.advancedban.utils.punishment.Identifier;

public abstract class Target implements CommandExecutor {
    protected final String name;
    protected final Identifier identifier;

    public Target(String name, Identifier identifier) {
        this.name = name;
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public abstract void kick(String reason);
}
