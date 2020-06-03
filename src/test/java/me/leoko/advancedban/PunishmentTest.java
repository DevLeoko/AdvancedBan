package me.leoko.advancedban;

import me.leoko.advancedban.manager.CommandManager;
import me.leoko.advancedban.manager.DatabaseManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.TimeManager;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;

import java.util.Arrays;
import java.util.List;
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
        Assert.assertEquals("Reason should match", "Doing some unit-testing", PunishmentManager.get().getBan("leoko").getReason());
    }

    @Test
    public void shouldKeepPunishmentAfterRestart(){
        System.out.println("Persistence test...");
        Punishment punishment = new Punishment("leoko", "leoko", "Persistence test", "JUnit5", PunishmentType.MUTE, TimeManager.getTime(), -1, null, -1);
        punishment.create();
        int id = punishment.getId();
        System.out.println("Punishment ID >> "+id);
        DatabaseManager.get().shutdown();
        DatabaseManager.get().setup(false);
        Punishment punishment1 = PunishmentManager.get().getPunishment(id);
        Assert.assertNotNull("Punishment should exist", punishment1);
        Assert.assertEquals("Reason should still match", "Persistence test", punishment1.getReason());
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
    
    @Test
    public void shouldBlockBasicCommandsIncludingColons() {
        Universal universal = Universal.get();
        List<String> muteCommands = Arrays.asList("msg", "reply", "tell");
        Assert.assertTrue("Command should be blocked as it matches exactly a mute command",
                universal.isMuteCommand("msg", muteCommands));
        Assert.assertTrue("Command should be blocked as a mute command regardless of colon",
                universal.isMuteCommand("plugin:reply", muteCommands));
        Assert.assertFalse("Command not in the mute commands list should not be blocked",
                universal.isMuteCommand("fly", muteCommands));
        Assert.assertFalse(
                "Command not in the mute commands list, but similar to a command in the list, should not be blocked",
                universal.isMuteCommand("replyall", muteCommands));
    }
    
    @Test
    public void shouldBlockCommandsStartingWithMuteCommandWords() {
        Universal universal = Universal.get();
        String muteCommand = "party msg";
        Assert.assertTrue("Subcommand with arguments should be blocked as a mute command",
                universal.muteCommandMatches("party msg user hello".split(" "), muteCommand));
        Assert.assertTrue("Subcommand without arguments should be blocked as a mute command",
                universal.muteCommandMatches("party msg".split(" "), muteCommand));

        Assert.assertFalse("Base command should not be blocked as a mute command although it partially matches",
                universal.muteCommandMatches("party".split(" "), muteCommand));

        Assert.assertFalse("Different subcommand with arguments should not be blocked as a mute command",
                universal.muteCommandMatches("party invite user".split(" "), muteCommand));
        Assert.assertFalse("Different subcommand without arguments should not be blocked as a mute command",
                universal.muteCommandMatches("party invite".split(" "), muteCommand));

        Assert.assertFalse("Different base command entirely should not be blocked as a mute command",
                universal.muteCommandMatches("broadcast party msg".split(" "), muteCommand));
    }

    @AfterAll
    public static void shutdownUniversal(){
        Universal.get().shutdown();
    }
}