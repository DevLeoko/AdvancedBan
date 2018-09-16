import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.command.AbstractCommand;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.punishment.Punishment;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created by Leo on 07.08.2017.
 */
public class TestAdvancedBan extends AdvancedBan {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private Path dataFolder;

    public TestAdvancedBan() {
        super(UUIDManager.FetcherMode.MIXED);
        try {
            folder.create();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataFolder = folder.newFolder("base").toPath();
    }

    @Override
    protected void onRegisterCommand(AbstractCommand command) {
        Assert.assertNotNull("Command was null", command);
        //Command executors are not being registered for testing
    }

    @Override
    protected void log(Level level, String msg) {
        Assert.assertNotNull("Log level was null", level);
        Assert.assertNotNull("Message was null", msg);
        System.out.println('[' + level.getName() + "] " + msg);
    }

    @Override
    public String getVersion() {
        return "TEST";
    }

    @Override
    public void executeCommand(String command) {
        Assert.assertNotNull("Command to execute was null", command);
        // Commands are not executed in test environment
    }

    @Override
    public Path getDataFolderPath() {
        return dataFolder;
    }

    @Override
    public void scheduleRepeatingAsyncTask(Runnable runnable, long delay, long period) {
        Assert.assertNotNull("Runnable was null", runnable);
        // Tasks are not available in test environment
    }

    @Override
    public void scheduleAsyncTask(Runnable runnable, long delay) {
        Assert.assertNotNull("Runnable was null", runnable);
        // Tasks are not available in test environment
    }

    @Override
    public void runAsyncTask(Runnable runnable) {
        Assert.assertNotNull("Runnable was null", runnable);
        // Tasks are not available in test environment
    }

    @Override
    public void runSyncTask(Runnable runnable) {
        Assert.assertNotNull("Runnable was null", runnable);
        // Tasks are not available in test environment
    }

    @Override
    public boolean isOnlineMode() {
        return false;
    }

    @Override
    public void callPunishmentEvent(Punishment punishment) {
        Assert.assertNotNull("Punishment was null", punishment);
        System.out.println("Called punishment event!");
    }

    @Override
    public void callRevokePunishmentEvent(Punishment punishment, boolean massClear) {
        Assert.assertNotNull("Punishment was null", punishment);
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