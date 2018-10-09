package me.leoko.advancedban;

import me.leoko.advancedban.command.AbstractCommand;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.punishment.Punishment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by Leo on 07.08.2017.
 */
public class TestAdvancedBan extends AdvancedBan {
    private Path dataFolder;

    public TestAdvancedBan(Path dataFolder) throws IOException {
        super(UUIDManager.FetcherMode.MIXED, true);
        this.dataFolder = dataFolder;
        Files.createDirectories(dataFolder);
    }

    @Override
    protected void onRegisterCommand(AbstractCommand command) {
        assertNotNull(command, "Command was null");
        //Command executors are not being registered for testing
    }

    @Override
    protected void log(Level level, String msg) {
        assertNotNull(level, "Log level was null");
        assertNotNull(msg, "Message was null");
        System.out.println('[' + level.getName() + "] " + msg);
    }

    @Override
    public String getVersion() {
        return "TEST";
    }

    @Override
    public void executeCommand(String command) {
        assertNotNull(command, "Command to execute was null");
        // Commands are not executed in test environment
    }

    @Override
    public Path getDataFolderPath() {
        return dataFolder;
    }

    @Override
    public void scheduleRepeatingAsyncTask(Runnable runnable, long delay, long period) {
        assertNotNull(runnable, "Runnable was null");
        // Tasks are not available in test environment
    }

    @Override
    public void scheduleAsyncTask(Runnable runnable, long delay) {
        assertNotNull(runnable, "Runnable was null");
        // Tasks are not available in test environment
    }

    @Override
    public void runAsyncTask(Runnable runnable) {
        assertNotNull(runnable, "Runnable was null");
        // Tasks are not available in test environment
    }

    @Override
    public void runSyncTask(Runnable runnable) {
        assertNotNull(runnable, "Runnable was null");
        // Tasks are not available in test environment
    }

    @Override
    public boolean isOnlineMode() {
        return false;
    }

    @Override
    public void callPunishmentEvent(Punishment punishment) {
        assertNotNull(punishment, "Punishment was null");
        System.out.println("Called punishment event!");
    }

    @Override
    public void callRevokePunishmentEvent(Punishment punishment, boolean massClear) {
        assertNotNull(punishment, "Punishment was null");
        System.out.println("Called punishment-revoke event!");
    }

    @Override
    public Optional<UUID> getInternalUUID(String name) {
        return Optional.empty();
    }

    @Override
    public boolean isUnitTesting() {
        return true;
    }
}