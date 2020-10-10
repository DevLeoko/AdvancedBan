package me.leoko.advancedban.utils.punishment;

import java.util.UUID;

public class Identifier {
    private final IdentifierType type;
    private final String value;

    public Identifier(IdentifierType type, String value) {
        this.type = type;
        this.value = value;
    }

    public static Identifier from(UUID uuid){
        return new Identifier(IdentifierType.UUID, uuid.toString());
    }
}
