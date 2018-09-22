package me.leoko.advancedban;

import me.leoko.advancedban.command.AbstractCommand;
import me.leoko.advancedban.punishment.InterimData;
import me.leoko.advancedban.punishment.Punishment;
import me.leoko.advancedban.punishment.PunishmentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Leo on 07.08.2017.
 */
public class PunishmentTest {
    private TestAdvancedBan advancedBan;

    @BeforeAll
    @ExtendWith(TempDirectory.class)
    public void onEnable(@TempDirectory.TempDir Path dataFolder) throws IOException {
        advancedBan = new TestAdvancedBan(dataFolder);
        advancedBan.onEnable();
    }

    @Test
    public void shouldCreatePunishmentForGivenUserWithGivenReason(){
        assertFalse(advancedBan.getPunishmentManager().isBanned("leoko"), "User should not be banned by default");
        AbstractCommand banCommand = advancedBan.getCommandManager().getCommand("ban").orElseThrow(() -> new AssertionError("Ban command was not found"));
        banCommand.execute(new TestCommandSender("UnitTest", advancedBan), new String[]{"Leoko", "Doing", "some", "unit-testing"});
        assertTrue(advancedBan.getPunishmentManager().isBanned("leoko"), "Punishment from above has failed");
        assertEquals(advancedBan.getPunishmentManager().getInterimBan("leoko").orElseThrow(() -> new AssertionError("Ban does not exist"))
                .getReason().orElseThrow(() -> new AssertionError("Reason does not exist")), "Doing some unit-testing", "Reason should match");
    }

    @Test
    public void shouldKeepPunishmentAfterRestart(){
        Punishment punishment = new Punishment("leoko", "leoko", "Testing", null, advancedBan.getTimeManager().getTime(), -1, PunishmentType.MUTE);
        punishment.setReason("Persistence test");
        advancedBan.getPunishmentManager().addPunishment(punishment);
        int id = punishment.getId().getAsInt();
        advancedBan.getDatabaseManager().onEnable();
        advancedBan.getDatabaseManager().onDisable();
        Optional<Punishment> punishment1 = advancedBan.getPunishmentManager().getPunishment(id);
        assertTrue(punishment1.isPresent(), "Punishment should exist");
        assertEquals(punishment1.orElseThrow(IllegalStateException::new).getReason()
                .orElseThrow(() -> new AssertionError("Reason does not exist")), "Persistence test", "Reason should still match");
    }

    @Test
    public void shouldWorkWithCachedAndNotCachedPunishments() throws UnknownHostException {
        Punishment punishment = new Punishment("cache", "Cache Testing", "Cache Testing", null, advancedBan.getTimeManager().getTime(), -1, PunishmentType.MUTE);
        advancedBan.getPunishmentManager().addPunishment(punishment);
        assertFalse(advancedBan.getPunishmentManager().getLoadedPunishments(false).contains(punishment), "Punishment should not be cached if user is not online");
        assertTrue(advancedBan.getPunishmentManager().isBanned("cache"), "Punishment should be active even if not in cache");
        InterimData data = advancedBan.getPunishmentManager().load(UUID.randomUUID(), "cache", InetAddress.getLocalHost());
        advancedBan.getPunishmentManager().acceptData(data);
        assertTrue(advancedBan.getPunishmentManager().getLoadedPunishments(false).stream().anyMatch(pt -> pt.getIdentifier().equals("cache")), "Punishment should be cached after user is loaded");
        assertTrue(advancedBan.getPunishmentManager().isBanned("cache"), "Punishment should be still active when in cache");
    }

    @AfterAll
    public void onDisable() {
        advancedBan.onDisable();
    }
}