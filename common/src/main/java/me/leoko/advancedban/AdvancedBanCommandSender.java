package me.leoko.advancedban;

public interface AdvancedBanCommandSender {

    AdvancedBan getAdvancedBan();

    String getName();

    void sendMessage(String message);

    default void sendCustomMessage(String path) {
        sendCustomMessage(path, true);
    }

    default void sendCustomMessage(String path, boolean prefix) {
        sendCustomMessage(path, prefix, new Object[0]);
    }

    default void sendCustomMessage(String path, boolean prefix, Object... parameters) {
        getAdvancedBan().getMessageManager().sendMessage(this, path, prefix, parameters);
    }

    boolean executeCommand(String command);

    boolean hasPermission(String permission);
}
