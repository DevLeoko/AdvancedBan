package me.leoko.advancedban;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestCommandSender implements AdvancedBanCommandSender {
    private final String name;
    private final AdvancedBan advancedBan;

    @Override
    public AdvancedBan getAdvancedBan() {
        return advancedBan;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void sendMessage(String message) {
        System.out.print("Message -> " + name + ": " + message);
    }

    @Override
    public boolean executeCommand(String command) {
        return true;
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }
}
