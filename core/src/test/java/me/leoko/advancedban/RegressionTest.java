package me.leoko.advancedban;

import com.sun.net.httpserver.HttpServer;
import me.leoko.advancedban.manager.CommandManager;
import me.leoko.advancedban.manager.DatabaseManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.TimeManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.manager.UpdateManager;
import me.leoko.advancedban.utils.Command;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;
import me.leoko.advancedban.utils.SQLQuery;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Every test in here covers a bug which has actually been present in the plugin.
 */
public class RegressionTest {

    @TempDir
    public static File dataFolder;

    private static TestMethods methods;

    @BeforeAll
    public static void setupUniversal() {
        methods = new TestMethods(dataFolder);
        Universal.get().setup(methods);
    }

    @AfterAll
    public static void shutdownUniversal() {
        Universal.get().shutdown();
    }

    @BeforeEach
    public void resetMethods() {
        methods.onlinePlayers.clear();
        methods.exemptPlayers.clear();
        methods.layouts.clear();
        methods.sentMessages.clear();
        methods.notifications.clear();
        methods.kickedPlayers.clear();
        methods.executedCommands.clear();
        methods.unitTesting = true;
    }

    private static void run(String command, String... args) {
        CommandManager.get().onCommand("UnitTest", command, args);
    }

    private static Punishment punishment(String target, PunishmentType type, long start, long end, String calculation) {
        return new Punishment(target, target, "Test", "JUnit", type, start, end, calculation, -1);
    }

    // --- Messages

    @Test
    public void reasonsMayContainCharactersWithSpecialMeaningInRegex() {
        methods.layouts.put("Warn.Notification", Collections.singletonList("%REASON%"));

        run("warn", "regexreason", "costs", "$5", "\\o/");

        assertEquals(Collections.singletonList("costs $5 \\o/"), methods.notifications);
        assertEquals(1, PunishmentManager.get().getCurrentWarns("regexreason"));
    }

    @Test
    public void warnActionsMayContainCharactersWithSpecialMeaningInRegex() {
        // The test setup claims to have a command for every warn count, so the first one gets used here
        methods.layouts.put("WarnActions.1", Collections.singletonList("punish %PLAYER% %REASON%"));

        new Punishment("regexaction", "regexaction", "$1 reason", "JUnit", PunishmentType.WARNING,
                TimeManager.getTime(), -1, null, -1).create(true);

        assertEquals(Collections.singletonList("punish regexaction $1 reason"), methods.executedCommands);
    }

    @Test
    public void emptyLayoutsDoNotBreakTheKickMessage() {
        assertEquals("", punishment("emptylayout", PunishmentType.BAN, TimeManager.getTime(), -1, null).getLayoutBSN());
    }

    // --- Command syntax

    @Test
    public void temporaryPunishmentsRequireSeparateNameAndTime() {
        Command tempban = Command.getByName("tempban");
        assertTrue(tempban.validateArguments(new String[]{"name", "1d"}));
        assertTrue(tempban.validateArguments(new String[]{"name", "2mo", "some", "reason"}));
        assertTrue(tempban.validateArguments(new String[]{"-s", "name", "1h", "reason"}));
        assertTrue(tempban.validateArguments(new String[]{"name", "-s", "1h", "reason"}));
        assertTrue(tempban.validateArguments(new String[]{"name", "#layout"}));

        assertFalse(tempban.validateArguments(new String[]{"name1d"}), "There is no time argument which could be read");
        assertFalse(tempban.validateArguments(new String[]{"11d"}), "There is no time argument which could be read");
    }

    @Test
    public void numbersWhichWouldOverflowAreRejectedBySyntax() {
        assertFalse(Command.getByName("tempban").validateArguments(new String[]{"name", "99999999999999999999d"}));
        assertFalse(Command.getByName("unpunish").validateArguments(new String[]{"99999999999"}));
        assertFalse(Command.getByName("unwarn").validateArguments(new String[]{"99999999999"}));
        assertFalse(Command.getByName("banlist").validateArguments(new String[]{"99999999999"}));
        assertFalse(Command.getByName("history").validateArguments(new String[]{"name", "99999999999"}));
        assertFalse(Command.getByName("change-reason").validateArguments(new String[]{"99999999999", "reason"}));
    }

    @Test
    public void largeWarnsPageIsNotParsedAsNumber() {
        // Gets treated as a name, parsing it as a page number would throw an exception
        assertDoesNotThrow(() -> run("warns", "99999999999"));
    }

    @Test
    public void silentTagAloneShowsUsage() {
        run("ban", "-s");
        run("kick", "-s");

        assertEquals(Arrays.asList("General.Prefix Ban.Usage", "General.Prefix Kick.Usage"), methods.sentMessages);
    }

    @Test
    public void silentTagWorksForKicks() {
        methods.onlinePlayers.add("silentkick");
        methods.layouts.put("Kick.Notification", Collections.singletonList("Kicked!"));

        run("kick", "-s", "silentkick", "reason");

        assertEquals(Collections.singletonList("silentkick"), methods.kickedPlayers);
        assertTrue(methods.notifications.isEmpty(), "Silent punishments should not be announced");
    }

    @Test
    public void onlyOnlinePlayersCanBeKicked() {
        run("kick", "offlinekick");

        assertEquals(Collections.singletonList("General.Prefix Kick.NotOnline"), methods.sentMessages);
        assertTrue(methods.kickedPlayers.isEmpty());
    }

    @Test
    public void shortIpBanAliasExists() {
        assertSame(Command.TEMP_IP_BAN, Command.getByName("tipban"));
    }

    // --- Exemptions

    @Test
    public void exemptPlayersCanNotBePunishedWhileOnline() {
        methods.exemptPlayers.add("ExemptOnline");
        methods.onlinePlayers.add("exemptonline");

        run("ban", "ExemptOnline");

        assertFalse(PunishmentManager.get().isBanned("exemptonline"));
        assertEquals(Collections.singletonList("General.Prefix Ban.Exempt"), methods.sentMessages);
    }

    @Test
    public void exemptPlayersCanNotBePunishedWhileOffline() {
        methods.exemptPlayers.add("ExemptOffline");

        run("ban", "ExemptOffline");

        assertFalse(PunishmentManager.get().isBanned("exemptoffline"));
    }

    // --- Listing

    @Test
    public void banlistRemovesExpiredPunishments() {
        long now = TimeManager.getTime();
        punishment("expiredlist", PunishmentType.TEMP_BAN, now - 2000, now - 1000, null).create(true);
        punishment("expiredlist2", PunishmentType.TEMP_BAN, now - 4000, now - 3000, null).create(true);

        assertDoesNotThrow(() -> run("banlist"), "Deleting while going through the list must not break the command");

        assertTrue(PunishmentManager.get().getPunishments(SQLQuery.SELECT_USER_PUNISHMENTS, "expiredlist").isEmpty());
        assertTrue(PunishmentManager.get().getPunishments(SQLQuery.SELECT_USER_PUNISHMENTS, "expiredlist2").isEmpty());
    }

    @Test
    public void listWithOnlyExpiredEntriesIsReportedAsEmpty() {
        long now = TimeManager.getTime();
        punishment("expiredwarns", PunishmentType.TEMP_WARNING, now - 2000, now - 1000, null).create(true);

        run("warns", "expiredwarns");

        assertEquals(Collections.singletonList("General.Prefix Warns.NoEntries"), methods.sentMessages);
    }

    // --- Cache

    @Test
    public void punishingOfflinePlayerDoesNotDuplicateHistoryOnJoin() {
        punishment("historydupe", PunishmentType.TEMP_BAN, TimeManager.getTime(), TimeManager.getTime() + 60000, "layout").create(true);

        PunishmentManager.get().load("historydupe", "historydupe", "127.0.0.2").accept();

        assertEquals(1, PunishmentManager.get().getCalculationLevel("historydupe", "layout"),
                "Punishments counted twice would make the next time-layout step harsher");
        assertEquals(1, PunishmentManager.get().getPunishments("historydupe", null, false).size());
    }

    @Test
    public void revokingRemovesAllCopiesFromCache() {
        punishment("muteddupe", PunishmentType.MUTE, TimeManager.getTime(), -1, null).create(true);
        PunishmentManager.get().load("muteddupe", "muteddupe", "127.0.0.3").accept();
        PunishmentManager.get().load("muteddupe", "muteddupe", "127.0.0.3").accept();
        assertTrue(PunishmentManager.get().isMuted("muteddupe"));

        PunishmentManager.get().getMute("muteddupe").delete();

        assertFalse(PunishmentManager.get().isMuted("muteddupe"), "A second copy in the cache would keep the player muted");
    }

    @Test
    public void banOfCachedIpIsVisibleWithoutPlayerBeingOnline() {
        PunishmentManager.get().load("ipcached", "ipcached", "10.1.2.3").accept();

        new Punishment("10.1.2.3", "10.1.2.3", "Test", "JUnit", PunishmentType.IP_BAN, TimeManager.getTime(), -1, null, -1).create(true);

        assertTrue(PunishmentManager.get().isBanned("10.1.2.3"));
    }

    @Test
    public void punishmentsOfUncachedPlayersAreNotKeptInMemory() {
        Punishment punishment = punishment("notcached", PunishmentType.NOTE, TimeManager.getTime(), -1, null);
        punishment.create(true);

        assertFalse(PunishmentManager.get().getLoadedHistory().contains(punishment));
        assertFalse(PunishmentManager.get().getLoadedPunishments(false).contains(punishment));
    }

    @Test
    public void cacheCanBeModifiedWhileBeingRead() {
        PunishmentManager.get().getLoadedPunishments(false).add(punishment("concurrentread", PunishmentType.NOTE, 1, -1, null));
        PunishmentManager.get().getLoadedPunishments(false).add(punishment("concurrentread", PunishmentType.NOTE, 2, -1, null));

        assertDoesNotThrow(() -> {
            long added = 3;
            for (Punishment ignored : PunishmentManager.get().getLoadedPunishments(false)) {
                if (added < 10) {
                    PunishmentManager.get().getLoadedPunishments(false).add(punishment("concurrentread", PunishmentType.NOTE, added++, -1, null));
                }
            }
        }, "Chat, login and command threads all use this cache at the same time");
    }

    @Test
    public void samePunishmentEqualsItselfAcrossInstances() {
        Punishment fromCommand = new Punishment("name", "same", "Reason", "JUnit", PunishmentType.MUTE, 5, -1, null, 1);
        Punishment fromDatabase = new Punishment("name", "same", "New reason", "JUnit", PunishmentType.MUTE, 5, -1, null, 2);

        assertEquals(fromCommand, fromDatabase);
        assertEquals(fromCommand.hashCode(), fromDatabase.hashCode());
        assertNotEquals(fromCommand, new Punishment("name", "same", "Reason", "JUnit", PunishmentType.TEMP_MUTE, 5, -1, null, 1));
        assertNotEquals(fromCommand, new Punishment("name", "other", "Reason", "JUnit", PunishmentType.MUTE, 5, -1, null, 1));
        assertNotEquals(fromCommand, new Punishment("name", "same", "Reason", "JUnit", PunishmentType.MUTE, 6, -1, null, 1));
    }

    // --- Database outage

    @Test
    public void databaseOutageDoesNotThrowExceptions() {
        DatabaseManager.get().shutdown();
        try {
            assertNull(PunishmentManager.get().getMute("outage"));
            assertNull(PunishmentManager.get().getPunishment(424242));
            assertEquals(0, PunishmentManager.get().getCalculationLevel("outage", "layout"));
            assertTrue(PunishmentManager.get().getPunishments(SQLQuery.SELECT_ALL_PUNISHMENTS).isEmpty());
            // The caller has to find out, or it would report a punishment which does not exist
            assertThrows(IllegalStateException.class, () -> punishment("outage", PunishmentType.MUTE, TimeManager.getTime(), -1, null).create(true));
            assertFalse(PunishmentManager.get().isCached("outage"));
        } finally {
            DatabaseManager.get().setup(false);
        }
    }

    // --- Commands

    @Test
    public void commandsConsistingOfSpacesAreNotMuteCommands() {
        assertFalse(Universal.get().isMuteCommand(" ", Collections.singletonList("msg")));
        assertFalse(Universal.get().isMuteCommand("", Collections.singletonList("msg")));
    }

    // --- JSON

    @Test
    public void parsingJsonReturnsNullWithoutValue() {
        assertEquals("abc", methods.parseJSON("{\"id\":\"abc\"}", "id"));
        assertEquals("5", methods.parseJSON("{\"id\":5}", "id"));

        assertNull(methods.parseJSON("{\"id\":null}", "id"), "Must not become the text 'null' which would be used as a UUID");
        assertNull(methods.parseJSON("{\"status\":\"ERR\"}", "id"));
        assertNull(methods.parseJSON("<html>Not found</html>", "id"));
        assertNull(methods.parseJSON("[1,2]", "id"));
        assertNull(methods.parseJSON("", "id"));
        assertNull(methods.parseJSON("{\"id\":{\"nested\":true}}", "id"));
    }

    @Test
    public void readingJsonFromUrlSupportsNestedKeys() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/ok", exchange -> {
            byte[] body = "{\"a\":{\"b\":\"value\"},\"missing\":null}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort();
            assertEquals("value", methods.getFromUrlJson(url + "/ok", "a|b"));
            assertNull(methods.getFromUrlJson(url + "/ok", "missing"));
            assertNull(methods.getFromUrlJson(url + "/not-found", "a"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void uuidsAreParsedWithAndWithoutDashes() {
        UUID uuid = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");
        assertEquals(uuid, UUIDManager.get().fromString("069a79f444e94726a5befca90e38aaf5"));
        assertEquals(uuid, UUIDManager.get().fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"));
        assertNull(UUIDManager.get().fromString("not-a-uuid"));
    }

    // --- Config updates

    @Test
    public void updateReplacesDeadUuidApisAndReloadsFiles() throws IOException {
        File config = new File(dataFolder, "config.yml");
        File messages = new File(dataFolder, "Messages.yml");
        FileUtils.writeLines(config, "UTF8", Arrays.asList(
                "UUID-Fetcher:",
                "  REST-API:",
                "    URL: \"https://api.mojang.com/users/profiles/minecraft/%NAME%?at=%TIMESTAMP%\"",
                "    Key: \"id\"",
                "  BackUp-API:",
                "    URL: \"https://us.mc-api.net/v3/uuid/%NAME%\"",
                "    Key: \"uuid\"",
                "DefaultReason: \"Grüße\""));
        FileUtils.writeLines(messages, "UTF8", Collections.singletonList("Check:"));

        methods.unitTesting = false;
        int loadsBefore = methods.loadFilesCalls;
        UpdateManager.get().setup();

        List<String> lines = FileUtils.readLines(config, StandardCharsets.UTF_8);
        assertTrue(lines.contains("    URL: \"https://api.mojang.com/users/profiles/minecraft/%NAME%\""));
        assertTrue(lines.contains("    URL: \"https://api.minetools.eu/uuid/%NAME%\""));
        assertFalse(lines.contains("    Key: \"uuid\""));
        assertEquals(2, Collections.frequency(lines, "    Key: \"id\""));
        assertTrue(lines.contains("DefaultReason: \"Grüße\""), "Files have to stay UTF-8 independently of the platform's charset");
        assertEquals(loadsBefore + 1, methods.loadFilesCalls, "Added options would be missing until the next restart otherwise");
    }
}
