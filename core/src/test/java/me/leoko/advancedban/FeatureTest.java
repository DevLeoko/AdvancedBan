package me.leoko.advancedban;

import me.leoko.advancedban.manager.AltManager;
import me.leoko.advancedban.manager.CommandManager;
import me.leoko.advancedban.manager.DatabaseManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.TimeManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.utils.Command;
import me.leoko.advancedban.utils.PlaceholderValues;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;
import me.leoko.advancedban.utils.SQLQuery;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Alt detection, punishment ladders and the values for placeholders.
 */
public class FeatureTest {

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
        methods.booleans.clear();
        methods.offlinePermissions.clear();
        methods.sentMessages.clear();
        methods.notifications.clear();
        methods.onlineMode = false;
    }

    private static void run(String command, String... args) {
        CommandManager.get().onCommand("UnitTest", command, args);
    }

    private static List<String> altNames(String uuid) {
        return AltManager.get().getAlts(uuid).stream().map(AltManager.Account::getName).collect(Collectors.toList());
    }

    private static void ladder(String offence, String reason, String... steps) {
        methods.layouts.put("Ladder." + offence + ".Steps", Arrays.asList(steps));
        if (reason != null) {
            methods.layouts.put("Ladder." + offence + ".Reason", Collections.singletonList(reason));
        }
    }

    private static PunishmentType activeType(String uuid, PunishmentType basic) {
        List<Punishment> punishments = PunishmentManager.get().getPunishments(uuid, basic, true);
        return punishments.isEmpty() ? null : punishments.get(0).getType();
    }

    // --- Alt detection

    @Test
    public void accountsSharingAnIpAreFoundInBothDirections() {
        Universal.get().callConnection("alta", "1.0.0.1");
        Universal.get().callConnection("altb", "1.0.0.1");
        Universal.get().callConnection("altc", "1.0.0.2");

        assertEquals(Collections.singletonList("altb"), altNames("alta"));
        assertEquals(Collections.singletonList("alta"), altNames("altb"));
        assertTrue(altNames("altc").isEmpty());
    }

    @Test
    public void accountsAreConnectedThroughAnyOfTheirIps() {
        Universal.get().callConnection("hop1", "1.0.1.1");
        Universal.get().callConnection("hop2", "1.0.1.2");
        Universal.get().callConnection("hop2", "1.0.1.1");

        assertEquals(Collections.singletonList("hop2"), altNames("hop1"));
        assertEquals(1, AltManager.get().getAccounts("1.0.1.2").size());
    }

    @Test
    public void repeatedLoginsDoNotDuplicateAccounts() {
        Universal.get().callConnection("repa", "1.0.2.1");
        Universal.get().callConnection("repb", "1.0.2.1");
        Universal.get().callConnection("repb", "1.0.2.1");

        assertEquals(Collections.singletonList("repb"), altNames("repa"));
        assertEquals(2, AltManager.get().getAccounts("1.0.2.1").size());
    }

    @Test
    public void ignoredIpsAreNotRemembered() {
        methods.layouts.put("AltDetection.IgnoredIPs", Collections.singletonList("1.0.3.1"));
        Universal.get().callConnection("igna", "1.0.3.1");
        Universal.get().callConnection("ignb", "1.0.3.1");

        assertTrue(altNames("igna").isEmpty());
        assertTrue(AltManager.get().getAccounts("1.0.3.1").isEmpty());
    }

    @Test
    public void ipsUsedByTooManyAccountsSayNothing() {
        // The default limit allows 10 accounts per IP
        for (int i = 0; i < 10; i++) {
            Universal.get().callConnection("shareda" + i, "1.0.4.1");
        }
        assertEquals(9, altNames("shareda0").size());

        Universal.get().callConnection("shareda10", "1.0.4.1");
        assertTrue(altNames("shareda0").isEmpty());
    }

    @Test
    public void oldLoginsAreForgotten() {
        long old = TimeManager.getTime() - TimeUnit.DAYS.toMillis(91);
        DatabaseManager.get().executeStatement(SQLQuery.INSERT_LOGIN, "oldlogin", "oldlogin", "1.0.5.1", old);
        Universal.get().callConnection("newlogin", "1.0.5.1");
        assertEquals(2, AltManager.get().getAccounts("1.0.5.1").size());

        AltManager.get().setup();

        assertEquals(Collections.singletonList("newlogin"),
                AltManager.get().getAccounts("1.0.5.1").stream().map(AltManager.Account::getName).collect(Collectors.toList()));
    }

    @Test
    public void disabledAltDetectionRecordsAndReportsNothing() {
        methods.booleans.put("AltDetection.Enabled", false);
        Universal.get().callConnection("offa", "1.0.6.1");
        Universal.get().callConnection("offb", "1.0.6.1");
        methods.booleans.remove("AltDetection.Enabled");

        assertTrue(altNames("offa").isEmpty());

        methods.booleans.put("AltDetection.Enabled", false);
        run("alts", "offa");
        assertEquals(Collections.singletonList("General.Prefix Alts.Disabled"), methods.sentMessages);
    }

    @Test
    public void joiningNextToABannedAccountAlertsTheStaffButIsAllowedByDefault() {
        methods.layouts.put("AltDetection.Notification", Arrays.asList("%NAME%", "%ALT%", "%REASON%"));
        Universal.get().callConnection("evaded", "1.0.7.1");
        run("ban", "evaded", "griefing");
        methods.notifications.clear();

        String result = Universal.get().callConnection("evader", "1.0.7.1");

        assertNull(result);
        assertEquals(Arrays.asList("evader", "evaded", "griefing"), methods.notifications);
        assertFalse(PunishmentManager.get().isBanned("evader"));
    }

    @Test
    public void noAlertWithoutABannedAccount() {
        methods.layouts.put("AltDetection.Notification", Collections.singletonList("alert"));
        Universal.get().callConnection("friend1", "1.0.8.1");

        assertNull(Universal.get().callConnection("friend2", "1.0.8.1"));
        assertTrue(methods.notifications.isEmpty());
    }

    @Test
    public void autoBanLastsAsLongAsTheEvadedBanAndDeniesTheConnection() {
        methods.onlineMode = true;
        methods.booleans.put("AltDetection.AutoBan.Enabled", true);
        methods.layouts.put("AltDetection.AutoBan.Reason", Collections.singletonList("alt of %ALT%"));
        Universal.get().callConnection("autosrc", "1.0.9.1");
        run("tempban", "autosrc", "2h", "griefing");
        Punishment source = PunishmentManager.get().getBan("autosrc");

        String result = Universal.get().callConnection("autoalt", "1.0.9.1");

        assertNotNull(result);
        Punishment evasion = PunishmentManager.get().getBan("autoalt");
        assertNotNull(evasion);
        assertEquals("alt of autosrc", evasion.getReason());
        assertEquals("AltDetection", evasion.getOperator());
        assertEquals(PunishmentType.TEMP_BAN, evasion.getType());
        assertEquals(source.getEnd(), evasion.getEnd());
        assertFalse(PunishmentManager.get().isCached("autoalt"), "A denied connection must not stay in the cache");
    }

    @Test
    public void exemptPlayersAreNeverAutoBanned() {
        methods.onlineMode = true;
        methods.booleans.put("AltDetection.AutoBan.Enabled", true);
        Universal.get().callConnection("exsrc", "1.0.10.1");
        run("ban", "exsrc", "griefing");

        methods.exemptPlayers.add("exlisted");
        assertNull(Universal.get().callConnection("exlisted", "1.0.10.1"));
        assertFalse(PunishmentManager.get().isBanned("exlisted"));

        methods.offlinePermissions.add("experm:ab.ban.exempt");
        assertNull(Universal.get().callConnection("experm", "1.0.10.1"));
        assertFalse(PunishmentManager.get().isBanned("experm"));
    }

    @Test
    public void autoBannedAccountsCanBeUnbanned() {
        methods.onlineMode = true;
        methods.booleans.put("AltDetection.AutoBan.Enabled", true);
        Universal.get().callConnection("unbansrc", "1.0.16.1");
        run("ban", "unbansrc", "griefing");
        assertNotNull(Universal.get().callConnection("unbanalt", "1.0.16.1"));

        run("unban", "unbanalt");

        assertNull(Universal.get().callConnection("unbanalt", "1.0.16.1"), "It would be impossible to unban otherwise");
        assertFalse(PunishmentManager.get().isBanned("unbanalt"));
        PunishmentManager.get().discard("unbanalt");
    }

    @Test
    public void autoBansDoNotSpreadToAccountsSharingAnIpWithAnAutoBannedOne() {
        methods.onlineMode = true;
        methods.booleans.put("AltDetection.AutoBan.Enabled", true);
        methods.layouts.put("AltDetection.Notification", Collections.singletonList("alert"));
        Universal.get().callConnection("chainb", "1.0.17.2");
        Universal.get().callConnection("chaina", "1.0.17.1");
        run("ban", "chaina", "griefing");
        assertNotNull(Universal.get().callConnection("chainb", "1.0.17.1"));
        methods.notifications.clear();

        // Only shares the other IP with the account which got banned automatically
        assertNull(Universal.get().callConnection("chainc", "1.0.17.2"));

        assertFalse(PunishmentManager.get().isBanned("chainc"));
        assertTrue(methods.notifications.isEmpty());
        PunishmentManager.get().discard("chainc");
    }

    @Test
    public void autoBanRequiresTheIpInUseRightNow() {
        methods.onlineMode = true;
        methods.booleans.put("AltDetection.AutoBan.Enabled", true);
        methods.layouts.put("AltDetection.Notification", Collections.singletonList("alert"));
        Universal.get().callConnection("oldipsrc", "1.0.18.1");
        Universal.get().callConnection("oldipalt", "1.0.18.1");
        run("ban", "oldipsrc", "griefing");
        methods.notifications.clear();

        assertNull(Universal.get().callConnection("oldipalt", "1.0.18.2"), "Coming from home now");

        assertFalse(PunishmentManager.get().isBanned("oldipalt"));
        assertEquals(Collections.singletonList("alert"), methods.notifications, "It's still worth telling the staff");
        PunishmentManager.get().discard("oldipalt");
    }

    @Test
    public void autoBanCanNotBeAbusedOnServersInOfflineMode() {
        methods.booleans.put("AltDetection.AutoBan.Enabled", true);
        Universal.get().callConnection("griefer", "1.0.19.1");
        run("ban", "griefer", "griefing");

        assertNull(Universal.get().callConnection("somevictim", "1.0.19.1"), "Anyone can use any name in offline mode");

        assertFalse(PunishmentManager.get().isBanned("somevictim"));
        PunishmentManager.get().discard("somevictim");
    }

    @Test
    public void ignoredIpv6AddressesMatchInAnySpelling() {
        methods.layouts.put("AltDetection.IgnoredIPs", Collections.singletonList("::1"));

        Universal.get().callConnection("loopback6", "0:0:0:0:0:0:0:1");
        Universal.get().callConnection("loopback6b", "0:0:0:0:0:0:0:1%lo");

        assertTrue(AltManager.get().getAccounts("::1").isEmpty());
        PunishmentManager.get().discard("loopback6");
        PunishmentManager.get().discard("loopback6b");
    }

    @Test
    public void altsCommandFindsIpv6Addresses() {
        methods.layouts.put("Alts.Entry", Collections.singletonList("%ALT%"));
        Universal.get().callConnection("sixer", "2001:db8:0:0:0:0:0:1");
        methods.sentMessages.clear();

        run("alts", "2001:db8::1");

        assertTrue(methods.sentMessages.contains("sixer"), methods.sentMessages.toString());
        PunishmentManager.get().discard("sixer");
    }

    @Test
    public void oldLoginsAreAlsoForgottenWhileTheServerIsRunning() throws ReflectiveOperationException {
        DatabaseManager.get().executeStatement(SQLQuery.INSERT_LOGIN, "stale", "stale", "1.0.20.1", TimeManager.getTime() - TimeUnit.DAYS.toMillis(91));
        Universal.get().callConnection("fresh", "1.0.20.1");
        assertEquals(2, AltManager.get().getAccounts("1.0.20.1").size(), "Checking on every login would be a waste");

        Field lastPurge = AltManager.class.getDeclaredField("lastPurge");
        lastPurge.setAccessible(true);
        lastPurge.setLong(AltManager.get(), TimeManager.getTime() - TimeUnit.HOURS.toMillis(2));
        Universal.get().callConnection("fresh", "1.0.20.1");

        assertEquals(1, AltManager.get().getAccounts("1.0.20.1").size());
        PunishmentManager.get().discard("fresh");
    }

    @Test
    public void aBrokenCheckDeniesTheConnectionInsteadOfLettingBannedPlayersIn() {
        Universal.get().callConnection("broken", "1.0.21.1");
        run("ban", "broken", "griefing");
        methods.brokenMessages = true;
        try {
            assertEquals("[AdvancedBan] Failed to load player data!", Universal.get().callConnection("broken", "1.0.21.1"));

            methods.booleans.put("LockdownOnError", false);
            assertNull(Universal.get().callConnection("broken", "1.0.21.1"));
        } finally {
            methods.brokenMessages = false;
            methods.booleans.remove("LockdownOnError");
        }
    }

    @Test
    public void textWithAColonWhichIsNoIpIsNotAProblemForTheAltsCommand() {
        methods.layouts.remove("Alts.Entry");
        assertDoesNotThrow(() -> run("alts", "no:address"));
        assertDoesNotThrow(() -> run("alts", "a:b:c"));
    }

    @Test
    public void theSilentTagIsNotTakenForTheNameOfTemporaryPunishments() {
        Command tempban = Command.getByName("tempban");
        assertFalse(tempban.validateArguments(new String[]{"-s", "1d"}));
        assertFalse(tempban.validateArguments(new String[]{"-S", "1d"}));
        assertFalse(tempban.validateArguments(new String[]{"-s", "1d", "reason"}));
        assertTrue(tempban.validateArguments(new String[]{"-S", "name", "1d"}));
        assertTrue(tempban.validateArguments(new String[]{"name", "-S", "1d"}));
    }

    @Test
    public void altsCommandListsAccountsWithTheirStatus() {
        methods.layouts.put("Alts.Entry", Collections.singletonList("%ALT%=%STATUS%"));
        methods.layouts.put("Alts.Banned", Collections.singletonList("banned"));
        methods.layouts.put("Alts.Clean", Collections.singletonList("clean"));
        Universal.get().callConnection("listme", "1.0.11.1");
        Universal.get().callConnection("listban", "1.0.11.1");
        run("ban", "listban", "x");
        Universal.get().callConnection("listok", "1.0.11.1");
        methods.sentMessages.clear();

        run("alts", "listme");
        assertTrue(methods.sentMessages.contains("listban=banned"), methods.sentMessages.toString());
        assertTrue(methods.sentMessages.contains("listok=clean"), methods.sentMessages.toString());

        methods.sentMessages.clear();
        run("alts", "1.0.11.1");
        assertTrue(methods.sentMessages.contains("listme=clean"), methods.sentMessages.toString());

        methods.sentMessages.clear();
        run("alts", "nobodyatall");
        assertEquals(Collections.singletonList("General.Prefix Alts.NoEntries"), methods.sentMessages);
    }

    @Test
    public void altsCommandSyntax() {
        Command alts = Command.getByName("alts");
        assertTrue(alts.validateArguments(new String[]{"name"}));
        assertFalse(alts.validateArguments(new String[]{}));
        assertFalse(alts.validateArguments(new String[]{"name", "more"}));
    }

    // --- Ladders

    @Test
    public void everyPunishmentForAnOffenceMovesUpTheLadderAndStaysOnTheLastStep() {
        ladder("Spam", "Spamming", "warn", "tempmute 1h", "mute");

        run("punish", "climber", "Spam");
        assertEquals(PunishmentType.WARNING, activeType("climber", PunishmentType.WARNING));
        assertNull(activeType("climber", PunishmentType.MUTE));

        run("punish", "climber", "Spam");
        Punishment mute = PunishmentManager.get().getMute("climber");
        assertEquals(PunishmentType.TEMP_MUTE, mute.getType());
        assertEquals("Spamming", mute.getReason());
        assertEquals(TimeUnit.HOURS.toMillis(1), mute.getEnd() - mute.getStart(), 5000, "The start gets determined slightly after the end");
        assertEquals("ladder:Spam", mute.getCalculation());
        mute.delete();

        run("punish", "climber", "Spam");
        assertEquals(PunishmentType.MUTE, activeType("climber", PunishmentType.MUTE));
        PunishmentManager.get().getMute("climber").delete();

        run("punish", "climber", "Spam");
        assertEquals(PunishmentType.MUTE, activeType("climber", PunishmentType.MUTE), "The last step stays");
    }

    @Test
    public void laddersAreCountedPerOffence() {
        ladder("First", null, "tempban 1d", "ban");
        ladder("Second", null, "warn", "kick");

        run("punish", "perladder", "First");
        run("punish", "perladder", "Second");

        assertEquals(PunishmentType.TEMP_BAN, activeType("perladder", PunishmentType.BAN));
        assertEquals(PunishmentType.WARNING, activeType("perladder", PunishmentType.WARNING));
    }

    @Test
    public void ladderProgressIsAlsoCountedForCachedPlayers() {
        ladder("Cached", null, "warn", "tempban 1d");
        methods.onlinePlayers.add("cachedclimber");
        Universal.get().callConnection("cachedclimber", "1.0.12.1");
        assertTrue(PunishmentManager.get().isCached("cachedclimber"));

        run("punish", "cachedclimber", "Cached");
        run("punish", "cachedclimber", "Cached");

        assertEquals(PunishmentType.TEMP_BAN, activeType("cachedclimber", PunishmentType.BAN));
        PunishmentManager.get().discard("cachedclimber");
    }

    @Test
    public void offencesAreFoundIgnoringCaseAndCountedTheSame() {
        ladder("Hacking", null, "warn", "tempmute 1d");

        run("punish", "casing", "hacking");
        run("punish", "casing", "HACKING");

        assertEquals(PunishmentType.TEMP_MUTE, activeType("casing", PunishmentType.MUTE));
    }

    @Test
    public void detailsAreAddedToTheConfiguredReasonAndTheSilentTagIsHonored() {
        ladder("Details", "Griefing", "warn", "warn");
        methods.layouts.put("Warn.Notification", Collections.singletonList("%REASON%"));

        run("punish", "detailed", "Details", "at", "spawn");
        assertEquals(Collections.singletonList("Griefing at spawn"), methods.notifications);
        assertEquals("Griefing at spawn", PunishmentManager.get().getWarns("detailed").get(0).getReason());

        methods.notifications.clear();
        run("punish", "-s", "detailed", "Details");
        assertTrue(methods.notifications.isEmpty());
        assertEquals(2, PunishmentManager.get().getCurrentWarns("detailed"));
    }

    @Test
    public void unknownOffencesListTheAvailableOnes() {
        methods.layouts.put("Punish.NotFound", Collections.singletonList("%NAME%: %OFFENCES%"));
        ladder("Known", null, "warn");

        run("punish", "someone", "unknown");

        assertEquals(Collections.singletonList("General.Prefix unknown: Known"), methods.sentMessages);
        assertEquals(0, PunishmentManager.get().getCurrentWarns("someone"));
    }

    @Test
    public void invalidStepsAreReportedInsteadOfPunishing() {
        for (String step : new String[]{"explode", "tempban", "tempban soon", "ban 1d", "ipban", "tempipban 1d", "tempban 0d"}) {
            methods.layouts.clear();
            methods.sentMessages.clear();
            ladder("Broken", null, step);

            run("punish", "invalidstep", "Broken");

            assertEquals(Collections.singletonList("General.Prefix Punish.InvalidStep"), methods.sentMessages, step);
            assertFalse(PunishmentManager.get().isBanned("invalidstep"), step);
        }
    }

    @Test
    public void unknownMessageLayoutsAreReportedInsteadOfPunishing() {
        ladder("Layouted", "@Missing", "ban");

        run("punish", "layoutless", "Layouted");

        assertEquals(Collections.singletonList("General.Prefix General.LayoutNotFound"), methods.sentMessages);
        assertFalse(PunishmentManager.get().isBanned("layoutless"));
    }

    @Test
    public void anAlreadyBannedPlayerDoesNotMoveUpTheLadder() {
        ladder("Again", null, "tempban 1d", "ban");
        run("punish", "banned1", "Again");
        run("punish", "banned1", "Again");

        assertEquals(PunishmentType.TEMP_BAN, activeType("banned1", PunishmentType.BAN));
        assertEquals(1, PunishmentManager.get().getCalculationLevel("banned1", "ladder:Again"));
    }

    @Test
    public void exemptPlayersAreNotPunishedByLadders() {
        ladder("Exempt", null, "ban");
        methods.exemptPlayers.add("protected1");

        run("punish", "protected1", "Exempt");

        assertFalse(PunishmentManager.get().isBanned("protected1"));
    }

    @Test
    public void kickStepsRequireAnOnlinePlayer() {
        ladder("Kicks", null, "kick");

        run("punish", "notonline", "Kicks");

        assertEquals(Collections.singletonList("General.Prefix Kick.NotOnline"), methods.sentMessages);
    }

    @Test
    public void punishCommandSyntax() {
        Command punish = Command.getByName("punish");
        assertTrue(punish.validateArguments(new String[]{"name", "offence"}));
        assertTrue(punish.validateArguments(new String[]{"-s", "name", "offence"}));
        assertTrue(punish.validateArguments(new String[]{"name", "-s", "offence"}));
        assertTrue(punish.validateArguments(new String[]{"name", "offence", "some", "details"}));
        assertFalse(punish.validateArguments(new String[]{"name"}));
        assertFalse(punish.validateArguments(new String[]{}));

        // "-s name" passes the syntax with the tag being the name, the command itself has to catch that
        run("punish", "-s", "name");
        assertEquals(Collections.singletonList("General.Prefix Punish.Usage"), methods.sentMessages);
    }

    @Test
    public void punishTabCompletionSuggestsOffences() {
        ladder("Hacking", null, "ban");
        ladder("Spam", null, "warn");
        Command punish = Command.getByName("punish");

        assertEquals(Arrays.asList("Hacking"), punish.getTabCompleter().onTabComplete("UnitTest", new String[]{"name", "Ha"}));
        assertEquals(Arrays.asList("Spam"), punish.getTabCompleter().onTabComplete("UnitTest", new String[]{"-s", "name", "S"}));
    }

    // --- Placeholders

    @Test
    public void placeholdersAnswerFromTheCacheWithoutTheDatabase() {
        Universal.get().callConnection("papionline", "1.0.13.1");
        run("tempmute", "papionline", "1h", "too loud");
        new Punishment("papionline", "papionline", "first", "JUnit", PunishmentType.WARNING, TimeManager.getTime() - 1, -1, "", -1).create(true);
        new Punishment("papionline", "papionline", "second", "JUnit", PunishmentType.WARNING, TimeManager.getTime(), -1, "", -1).create(true);
        run("note", "papionline", "watch");

        PlaceholderValues values = PlaceholderValues.fromCache("papionline");

        assertEquals("yes", values.get("is_muted", "yes", "no"));
        assertEquals("no", values.get("is_banned", "yes", "no"));
        assertEquals("2", values.get("warns", "yes", "no"));
        assertEquals("1", values.get("notes", "yes", "no"));
        assertEquals("too loud", values.get("mute_reason", "yes", "no"));
        assertEquals("UnitTest", values.get("mute_operator", "yes", "no"));
        assertEquals("", values.get("ban_reason", "yes", "no"));
        assertNotNull(values.get("mute_end", "yes", "no"));
        assertNotNull(values.get("mute_remaining", "yes", "no"));
        PunishmentManager.get().discard("papionline");
    }

    @Test
    public void connectedPlayersCanBeResolvedWithoutALookup() {
        assertNull(UUIDManager.get().getInMemoryUUID("papiresolved"));

        Universal.get().callConnection("PapiResolved", "1.0.15.1");

        assertEquals("papiresolved", UUIDManager.get().getInMemoryUUID("papiresolved"));
        assertTrue(PunishmentManager.get().isCached(UUIDManager.get().getInMemoryUUID("papiresolved")));
        PunishmentManager.get().discard("papiresolved");
    }

    @Test
    public void placeholdersIgnoreExpiredPunishmentsInTheCache() {
        Universal.get().callConnection("papiexpired", "1.0.14.1");
        new Punishment("papiexpired", "papiexpired", "old", "JUnit", PunishmentType.TEMP_MUTE,
                TimeManager.getTime() - 2000, TimeManager.getTime() - 1000, "", -1).create(true);

        assertEquals("no", PlaceholderValues.fromCache("papiexpired").get("is_muted", "yes", "no"));
        PunishmentManager.get().discard("papiexpired");
    }

    @Test
    public void placeholdersCanBeReadFromTheDatabaseForOfflinePlayers() {
        run("ban", "papioffline", "griefing");

        PlaceholderValues values = PlaceholderValues.fromDatabase("papioffline");

        assertEquals("yes", values.get("is_banned", "yes", "no"));
        assertEquals("griefing", values.get("ban_reason", "yes", "no"));
        assertEquals(values.get("ban_remaining", "yes", "no"), values.get("ban_end", "yes", "no"), "Permanent bans have no end date");
    }

    @Test
    public void unknownPlaceholdersAreNotAnswered() {
        assertNull(PlaceholderValues.EMPTY.get("nonsense", "yes", "no"));
        assertNull(PlaceholderValues.EMPTY.get("ban_nonsense", "yes", "no"));
        assertNull(PlaceholderValues.EMPTY.get("mute_", "yes", "no"));
        assertEquals("", PlaceholderValues.EMPTY.get("ban_reason", "yes", "no"));
        assertEquals("0", PlaceholderValues.EMPTY.get("warns", "yes", "no"));
    }
}
