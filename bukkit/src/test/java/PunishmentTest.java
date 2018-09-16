import me.leoko.advancedban.command.AbstractCommand;
import me.leoko.advancedban.punishment.Punishment;
import me.leoko.advancedban.punishment.PunishmentType;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Leo on 07.08.2017.
 */
public class PunishmentTest {
    private final TestAdvancedBan advancedBan = new TestAdvancedBan();

    @BeforeAll
    public void onEnable() {
        advancedBan.onEnable();
    }

    @Test
    public void shouldCreatePunishmentForGivenUserWithGivenReason(){
        Assert.assertFalse("User should not be banned by default", advancedBan.getPunishmentManager().isBanned("leoko"));
        AbstractCommand banCommand = advancedBan.getCommandManager().getCommand("ban").orElseThrow(() -> new AssertionError("Ban command was not found"));
        banCommand.execute(new TestCommandSender("UnitTest", advancedBan), new String[]{"Leoko", "Doing", "some", "unit-testing"});
        Assert.assertTrue("Punishment from above has failed", advancedBan.getPunishmentManager().isBanned("leoko"));
        Assert.assertEquals("Reason should match",
                advancedBan.getPunishmentManager().getBan("leoko").orElseThrow(() -> new AssertionError("Ban does not exist"))
                        .getReason(), "Doing some unit-testing");
    }

    @Test
    public void shouldKeepPunishmentAfterRestart(){
        Punishment punishment = new Punishment(advancedBan, "leoko", "leoko", "Testing", null, advancedBan.getTimeManager().getTime(), -1, PunishmentType.MUTE);
        punishment.setReason("Persistence test");
        punishment.create();
        int id = punishment.getId();
        advancedBan.getDatabaseManager().onEnable();
        advancedBan.getDatabaseManager().onDisable();
        Optional<Punishment> punishment1 = advancedBan.getPunishmentManager().getPunishment(id);
        Assert.assertTrue("Punishment should exist", punishment1.isPresent());
        Assert.assertEquals("Reason should still match", punishment1.get().getReason(), "Persistence test");
    }

    @Test
    public void shouldWorkWithCachedAndNotCachedPunishments() throws UnknownHostException {
        Punishment punishment = new Punishment(advancedBan, "cache", "Cache Testing", "Cache Testing", null, advancedBan.getTimeManager().getTime(), -1, PunishmentType.MUTE);
        punishment.create();
        Assert.assertFalse("Punishment should not be cached if user is not online", advancedBan.getPunishmentManager().getLoadedPunishments(false).contains(punishment));
        Assert.assertTrue("Punishment should be active even if not in cache", advancedBan.getPunishmentManager().isBanned("cache"));
        advancedBan.getPunishmentManager().load(UUID.randomUUID(), "cache", InetAddress.getLocalHost()).accept(advancedBan.getPunishmentManager());
        Assert.assertTrue("Punishment should be cached after user is loaded", advancedBan.getPunishmentManager().getLoadedPunishments(false).stream().anyMatch(pt -> pt.getIdentifier().equals("cache")));
        Assert.assertTrue("Punishment should be still active when in cache", advancedBan.getPunishmentManager().isBanned("cache"));
    }

    @AfterAll
    public void onDisable() {
        advancedBan.onDisable();
    }
}