package me.leoko.advancedban;

import me.leoko.advancedban.manager.CommandManager;
import me.leoko.advancedban.manager.DatabaseManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.TimeManager;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Created by Leo on 07.08.2017.
 */
public class PunishmentTest {
    @BeforeAll
    public static void setupUniversal(){
        Universal.get().setup(new TestMethods());
    }

    @Test
    public void shouldCreatePunishmentForGivenUserWithGivenReason(){
        Assert.assertFalse("User should not be banned by default", PunishmentManager.get().isBanned("leoko"));
        CommandManager.get().onCommand("UnitTest", "ban", new String[]{"Leoko", "Doing", "some", "unit-testing"});
        Assert.assertTrue("Punishment from above has failed", PunishmentManager.get().isBanned("leoko"));
        Assert.assertEquals("Reason should match", PunishmentManager.get().getBan("leoko").getReason(), "Doing some unit-testing");
    }

    @Test
    public void shouldKeepPunishmentAfterRestart(){
        Punishment punishment = new Punishment("leoko", "leoko", "Persistance test", "JUnit5", PunishmentType.MUTE, TimeManager.getTime(), -1, null, -1);
        punishment.create();
        int id = punishment.getId();
        DatabaseManager.get().shutdown();
        DatabaseManager.get().setup(false);
        Punishment punishment1 = PunishmentManager.get().getPunishment(id);
        Assert.assertNotNull("Punishment should exist", punishment1);
        Assert.assertEquals("Reason should still match", punishment1.getReason(), "Persistance test");
    }

    @Test
    public void shouldWorkWithCachedAndNotCachedPunishments(){
        Punishment punishment = new Punishment("cache", "cache", "Cache test", "JUnit5", PunishmentType.BAN, TimeManager.getTime(), -1, null, -1);
        punishment.create();
        Assert.assertFalse("Punishment should not be cached if user is not online", PunishmentManager.get().getLoadedPunishments(false).contains(punishment));
        Assert.assertTrue("Punishment should be active even if not in cache", PunishmentManager.get().isBanned("cache"));
        PunishmentManager.get().load("cache", "cache", "127.0.0.1").accept();
        Assert.assertTrue("Punishment should be cached after user is loaded", PunishmentManager.get().getLoadedPunishments(false).stream().anyMatch(pt -> pt.getUuid().equals("cache")));
        Assert.assertTrue("Punishment should be still active when in cache", PunishmentManager.get().isBanned("cache"));
    }

    @AfterAll
    public static void shutdownUniversal(){
        Universal.get().shutdown();
    }
}