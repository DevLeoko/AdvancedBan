package me.leoko.advancedban.bungee;

import io.netty.buffer.ByteBuf;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import static net.md_5.bungee.protocol.DefinedPacket.*;

public class RedisMessageUtils {
    public static final int CONNECTION = 0;
    public static final int KICK = 1;
    public static final int NOTIFICATION = 2;
    public static final int MESSAGE = 3;

    public static void writeConnectionMessage(ByteBuf buf, String name, UUID uuid, InetAddress address) {
        writeVarInt(CONNECTION, buf);
        writeString(name, buf);
        writeUUID(uuid, buf);
        writeArray(address.getAddress(), buf);
    }


    public static InetAddress readAddress(ByteBuf buf) {
        byte[] address = readArray(buf);

        try {
            return InetAddress.getByAddress(address);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Unable to create InetAddress", e);
        }
    }
}
