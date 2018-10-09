package me.leoko.advancedban.punishment;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.net.InetAddress;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

/**
 * Created by Leoko @ dev.skamps.eu on 30.05.2016.
 */
@Data
public class Punishment {
    private final Object identifier;
    private final String name;
    private final String operator;
    private final String calculation;
    private final long start;
    private final long end;
    private final PunishmentType type;

    private String reason;
    @Setter(value = AccessLevel.PACKAGE)
    private int id = -1;

    public Punishment(@Nonnull Object identifier, String name, String operator, String calculation, long start, long end, @Nonnull PunishmentType type) {
        if (!(identifier instanceof UUID || identifier instanceof InetAddress)) {
            throw new IllegalArgumentException("identifier must be UUID or InetAddress");
        }
        this.identifier = Objects.requireNonNull(identifier);
        this.name = name;
        this.operator = operator;
        this.calculation = calculation;
        this.start = start;
        this.end = end;
        this.type = Objects.requireNonNull(type);
    }

    public Optional<String> getReason() {
        return Optional.ofNullable(reason);
        //return (reason == null ? advancedBan.getConfiguration().getDefaultReason() : reason).replaceAll("'", "");
    }

    public OptionalInt getId() {
        return id < 0 ? OptionalInt.empty() : OptionalInt.of(id);
    }
}
