package me.leoko.advancedban.utils;

import me.leoko.advancedban.manager.DatabaseManager;

/**
 * Created by Leo on 29.07.2017.
 */
public enum SQLQuery {
    CREATE_TABLE_PUNISHMENT(
            "CREATE TABLE IF NOT EXISTS `Punishments` ("+
            "`id` int NOT NULL AUTO_INCREMENT," +
            "`name` VARCHAR(16) NULL DEFAULT NULL," +
            "`uuid` VARCHAR(35) NULL DEFAULT NULL," +
            "`reason` VARCHAR(255) NULL DEFAULT NULL," +
            "`operator` VARCHAR(16) NULL DEFAULT NULL," +
            "`punishmentType` VARCHAR(16) NULL DEFAULT NULL," +
            "`start` LONG DEFAULT NULL," +
            "`end` LONG DEFAULT NULL," +
            "`calculation` VARCHAR(50) NULL DEFAULT NULL," +
            "PRIMARY KEY (`id`))",

            "CREATE TABLE IF NOT EXISTS Punishments (" +
            "id INTEGER IDENTITY PRIMARY KEY," +
            "name VARCHAR(16)," +
            "uuid VARCHAR(35)," +
            "reason VARCHAR(255)," +
            "operator VARCHAR(16)," +
            "punishmentType VARCHAR(16)," +
            "start BIGINT," +
            "end BIGINT," +
            "calculation VARCHAR(50))"
    ),
    CREATE_TABLE_PUNISHMENT_HISTORY(
            "CREATE TABLE IF NOT EXISTS `PunishmentHistory` (" +
            "`id` int NOT NULL AUTO_INCREMENT," +
            "`name` VARCHAR(16) NULL DEFAULT NULL," +
            "`uuid` VARCHAR(35) NULL DEFAULT NULL," +
            "`reason` VARCHAR(255) NULL DEFAULT NULL," +
            "`operator` VARCHAR(16) NULL DEFAULT NULL," +
            "`punishmentType` VARCHAR(16) NULL DEFAULT NULL," +
            "`start` LONG DEFAULT NULL," +
            "`end` LONG DEFAULT NULL," +
            "`calculation` VARCHAR(50) NULL DEFAULT NULL," +
            "PRIMARY KEY (`id`))",

            "CREATE TABLE IF NOT EXISTS PunishmentHistory (" +
            "id INTEGER IDENTITY PRIMARY KEY," +
            "name VARCHAR(16)," +
            "uuid VARCHAR(35)," +
            "reason VARCHAR(255)," +
            "operator VARCHAR(16)," +
            "punishmentType VARCHAR(16)," +
            "start BIGINT," +
            "end BIGINT," +
            "calculation VARCHAR(50))"
    ),
    CREATE_TABLE_LOGIN_HISTORY(
            "CREATE TABLE IF NOT EXISTS `LoginHistory` (" +
            "`uuid` VARCHAR(35) NOT NULL," +
            "`name` VARCHAR(16) NULL DEFAULT NULL," +
            "`ip` VARCHAR(45) NOT NULL," +
            "`lastSeen` BIGINT DEFAULT NULL," +
            "PRIMARY KEY (`uuid`, `ip`)," +
            "INDEX (`ip`))",

            "CREATE TABLE IF NOT EXISTS LoginHistory (" +
            "uuid VARCHAR(35) NOT NULL," +
            "name VARCHAR(16)," +
            "ip VARCHAR(45) NOT NULL," +
            "lastSeen BIGINT," +
            "PRIMARY KEY (uuid, ip))"
    ),
    // MySQL gets this as a part of the table
    CREATE_INDEX_LOGIN_HISTORY_IP(
            "SELECT 1",
            "CREATE INDEX IF NOT EXISTS LoginHistoryIp ON LoginHistory (ip)"
    ),
    INSERT_LOGIN(
            "INSERT INTO `LoginHistory` (`uuid`, `name`, `ip`, `lastSeen`) VALUES (?, ?, ?, ?)",
            "INSERT INTO LoginHistory (uuid, name, ip, lastSeen) VALUES (?, ?, ?, ?)"
    ),
    DELETE_LOGIN(
            "DELETE FROM `LoginHistory` WHERE `uuid` = ? AND `ip` = ?",
            "DELETE FROM LoginHistory WHERE uuid = ? AND ip = ?"
    ),
    DELETE_OLD_LOGINS(
            "DELETE FROM `LoginHistory` WHERE `lastSeen` < ?",
            "DELETE FROM LoginHistory WHERE lastSeen < ?"
    ),
    SELECT_LOGINS_BY_IP(
            "SELECT * FROM `LoginHistory` WHERE `ip` = ?",
            "SELECT * FROM LoginHistory WHERE ip = ?"
    ),
    SELECT_LOGIN_ALTS(
            "SELECT * FROM `LoginHistory` WHERE `ip` IN (SELECT `ip` FROM `LoginHistory` WHERE `uuid` = ?) AND `uuid` <> ?",
            "SELECT * FROM LoginHistory WHERE ip IN (SELECT ip FROM LoginHistory WHERE uuid = ?) AND uuid <> ?"
    ),
    INSERT_PUNISHMENT(
            "INSERT INTO `Punishments` " +
            "(`name`, `uuid`, `reason`, `operator`, `punishmentType`, `start`, `end`, `calculation`) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",

            "INSERT INTO Punishments " +
            "(name, uuid, reason, operator, punishmentType, start, end, calculation) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
    ),
    INSERT_PUNISHMENT_HISTORY(
            "INSERT INTO `PunishmentHistory` " +
            "(`name`, `uuid`, `reason`, `operator`, `punishmentType`, `start`, `end`, `calculation`) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",

            "INSERT INTO PunishmentHistory " +
            "(name, uuid, reason, operator, punishmentType, start, end, calculation) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
    ),
    SELECT_EXACT_PUNISHMENT(
            "SELECT * FROM `Punishments` WHERE `uuid` = ? AND `start` = ? AND `punishmentType` = ?",
            "SELECT * FROM Punishments WHERE uuid = ? AND start = ? AND punishmentType = ?"
    ),
    DELETE_PUNISHMENT(
            "DELETE FROM `Punishments` WHERE `id` = ?",
            "DELETE FROM Punishments WHERE id = ?"
    ),
    DELETE_OLD_PUNISHMENTS(
            "DELETE FROM `Punishments` WHERE `end` <= ? AND `end` != -1",
            "DELETE FROM Punishments WHERE end <= ? AND end != -1"
    ),
    SELECT_USER_PUNISHMENTS(
            "SELECT * FROM `Punishments` WHERE `uuid` = ?",
            "SELECT * FROM Punishments WHERE uuid = ?"
    ),
    SELECT_USER_PUNISHMENTS_HISTORY(
            "SELECT * FROM `PunishmentHistory` WHERE `uuid` = ?",
            "SELECT * FROM PunishmentHistory WHERE uuid = ?"
    ),
    SELECT_USER_PUNISHMENTS_WITH_IP(
            "SELECT * FROM `Punishments` WHERE `uuid` = ? OR `uuid` = ?",
            "SELECT * FROM Punishments WHERE uuid = ? OR uuid = ?"
    ),
    SELECT_USER_PUNISHMENTS_HISTORY_WITH_IP(
            "SELECT * FROM `PunishmentHistory` WHERE `uuid` = ? OR `uuid` = ?",
            "SELECT * FROM PunishmentHistory WHERE uuid = ? OR uuid = ?"
    ),
    SELECT_USER_PUNISHMENTS_HISTORY_BY_CALCULATION(
            "SELECT * FROM `PunishmentHistory` WHERE `uuid` = ? AND `calculation` = ?",
            "SELECT * FROM PunishmentHistory WHERE uuid = ? AND calculation = ?"
    ),
    UPDATE_PUNISHMENT_REASON(
            "UPDATE `Punishments` SET `reason` = ? WHERE `id` = ?",
            "UPDATE Punishments SET reason = ? WHERE id = ?"
    ),
    SELECT_PUNISHMENT_BY_ID(
            "SELECT * FROM `Punishments` WHERE `id` = ?",
            "SELECT * FROM Punishments WHERE id = ?"
    ),
    SELECT_ALL_PUNISHMENTS(
            "SELECT * FROM `Punishments`",
            "SELECT * FROM Punishments"
    ),
    SELECT_ALL_PUNISHMENTS_HISTORY(
            "SELECT * FROM `PunishmentHistory`",
            "SELECT * FROM PunishmentHistory"
    ),
    SELECT_ALL_PUNISHMENTS_LIMIT(
            "SELECT * FROM `Punishments` ORDER BY `start` DESC LIMIT ?",
            "SELECT * FROM Punishments ORDER BY start DESC LIMIT ?"
    ),
    SELECT_ALL_PUNISHMENTS_HISTORY_LIMIT(
            "SELECT * FROM `PunishmentHistory` ORDER BY `start` DESC LIMIT ?",
            "SELECT * FROM PunishmentHistory ORDER BY start DESC LIMIT ?"
    );

    private String mysql;
    private String hsqldb;

    SQLQuery(String mysql, String hsqldb) {
        this.mysql = mysql;
        this.hsqldb = hsqldb;
    }

    @Override
    public String toString() {
        return DatabaseManager.get().isUseMySQL() ? mysql : hsqldb;
    }
}
