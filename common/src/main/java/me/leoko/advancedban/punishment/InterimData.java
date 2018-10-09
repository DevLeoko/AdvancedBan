package me.leoko.advancedban.punishment;

import lombok.Value;

import java.net.InetAddress;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Leo on 04.08.2017.
 */
@Value
public class InterimData {
    private final UUID uuid;
    private final String name;
    private final InetAddress address;
    private final Set<Punishment> punishments;
    private final Set<Punishment> history;
}