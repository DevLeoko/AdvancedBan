package me.leoko.advancedban;

import me.leoko.advancedban.manager.CommandManager;
import me.leoko.advancedban.manager.DatabaseManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.TimeManager;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Leo on 07.08.2017.
 */
public class PunishmentTest {

    @TempDir
    public static File dataFolder;

    @BeforeAll
    public static void setupUniversal(){
        Universal.get().setup(new TestMethods(dataFolder));
    }

    @Test
    public void shouldCreatePunishmentForGivenUserWithGivenReason(){
        assertFalse(PunishmentManager.get().isBanned("leoko"), "User should not be banned by default");
        CommandManager.get().onCommand("UnitTest", "ban", new String[]{"Leoko", "Doing", "some", "unit-testing"});
        assertTrue(PunishmentManager.get().isBanned("leoko"), "Punishment from above has failed");
        assertEquals("Doing some unit-testing", PunishmentManager.get().getBan("leoko").getReason(), "Reason should match");
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
        assertNotNull(punishment1, "Punishment should exist");
        assertEquals("Persistence test", punishment1.getReason(), "Reason should still match");
    }

    @Test
    public void shouldWorkWithCachedAndNotCachedPunishments(){
        Punishment punishment = new Punishment("cache", "cache", "Cache test", "JUnit5", PunishmentType.BAN, TimeManager.getTime(), -1, null, -1);
        punishment.create();
        //assertFalse(PunishmentManager.get().getLoadedPunishments(false).contains(punishment), "Punishment should not be cached if user is not online");
        assertTrue(PunishmentManager.get().isBanned("cache"), "Punishment should be active even if not in cache");
        PunishmentManager.get().load("cache", "cache", "127.0.0.1").accept();
        assertTrue(PunishmentManager.get().getLoadedPunishments(false).stream().anyMatch(pt -> pt.getUuid().equals("cache")),
                "Punishment should be cached after user is loaded");
        assertTrue(PunishmentManager.get().isBanned("cache"), "Punishment should be still active when in cache");
    }
    
    @Test
    public void shouldBlockBasicCommandsIncludingColons() {
        Universal universal = Universal.get();
        List<String> muteCommands = Arrays.asList("msg", "reply", "tell");
        assertTrue(universal.isMuteCommand("msg", muteCommands),
                "Command should be blocked as it matches exactly a mute command");
        assertTrue(universal.isMuteCommand("plugin:reply", muteCommands),
                "Command should be blocked as a mute command regardless of colon");
        assertFalse(universal.isMuteCommand("fly", muteCommands),
                "Command not in the mute commands list should not be blocked");
        assertFalse(universal.isMuteCommand("replyall", muteCommands),
                "Command not in the mute commands list, but similar to a command in the list, should not be blocked");
    }
    
    @Test
    public void shouldBlockCommandsStartingWithMuteCommandWords() {
        Universal universal = Universal.get();
        String muteCommand = "party msg";
        assertTrue(universal.muteCommandMatches("party msg user hello".split(" "), muteCommand),
                "Subcommand with arguments should be blocked as a mute command");
        assertTrue(universal.muteCommandMatches("party msg".split(" "), muteCommand),
                "Subcommand without arguments should be blocked as a mute command");

        assertFalse(universal.muteCommandMatches("party".split(" "), muteCommand),
                "Base command should not be blocked as a mute command although it partially matches");

        assertFalse(universal.muteCommandMatches("party invite user".split(" "), muteCommand),
                "Different subcommand with arguments should not be blocked as a mute command");
        assertFalse(universal.muteCommandMatches("party invite".split(" "), muteCommand),
                "Different subcommand without arguments should not be blocked as a mute command");

        assertFalse(universal.muteCommandMatches("broadcast party msg".split(" "), muteCommand),
                "Different base command entirely should not be blocked as a mute command");
    }

    @AfterAll
    public static void shutdownUniversal(){
        Universal.get().shutdown();
    }
}