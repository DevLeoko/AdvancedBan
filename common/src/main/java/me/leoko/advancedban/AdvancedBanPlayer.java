package me.leoko.advancedban;

import java.net.InetSocketAddress;
import java.util.UUID;

public interface AdvancedBanPlayer extends AdvancedBanCommandSender {

    InetSocketAddress getAddress();

    UUID getUniqueId();

    void kick(String reason);
}
