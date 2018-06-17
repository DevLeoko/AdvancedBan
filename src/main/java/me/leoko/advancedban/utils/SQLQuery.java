package me.leoko.advancedban.utils;

import me.leoko.advancedban.manager.DatabaseManager;

/**
 * Created by Leo on 29.07.2017.
 */
public enum SQLQuery {
    CREATE_TABLE_PUNISHMENT(
            "CREATE TABLE IF NOT EXISTS `Punishments` (" +
            "`id` INT NOT NULL AUTO_INCREMENT, " +
            "`name` VARCHAR(16) NOT NULL, " +
            "`uuid` VARCHAR(32) NOT NULL, " +
            "`reason` TEXT NOT NULL, " +
            "`operator` VARCHAR(16) NOT NULL, " +
            "`punishmentType` " + PunishmentType.getAsMysqlEnum() + " NOT NULL, " +
            "`start` TIMESTAMP(3) NOT NULL, " +
            "`end` TIMESTAMP(3) NULL DEFAULT NULL, " +
            "`calculation` TINYTEXT NULL DEFAULT NULL, " +
            "PRIMARY KEY (`id`), " + 
            "KEY `uuid` (`uuid`), " + 
            "KEY `start` (`start`)" + 
            ") DEFAULT CHARSET=utf8mb4",

            "CREATE TABLE IF NOT EXISTS Punishments (" +
            "id INTEGER IDENTITY PRIMARY KEY, " +
            "name VARCHAR(16), " +
            "uuid VARCHAR(32), " +
            "reason VARCHAR(100), " +
            "operator VARCHAR(16), " +
            "punishmentType VARCHAR(16), " +
            "start BIGINT, " +
            "end BIGINT, " +
            "calculation VARCHAR(50))"
    ),
    CREATE_TABLE_PUNISHMENT_HISTORY(
            "CREATE TABLE IF NOT EXISTS `PunishmentHistory` (" +
            "`id` INT NOT NULL AUTO_INCREMENT, " +
            "`name` VARCHAR(16) NOT NULL, " +
            "`uuid` VARCHAR(32) NOT NULL, " +
            "`reason` TEXT NOT NULL, " +
            "`operator` VARCHAR(16) NOT NULL, " +
            "`punishmentType` " + PunishmentType.getAsMysqlEnum() + " NOT NULL, " +
            "`start` TIMESTAMP(3) NOT NULL, " +
            "`end` TIMESTAMP(3) NULL DEFAULT NULL, " +
            "`calculation` TINYTEXT NULL DEFAULT NULL, " +
            "PRIMARY KEY (`id`), " + 
            "KEY `uuid` (`uuid`), " + 
            "KEY `start` (`start`)" + 
            ") DEFAULT CHARSET=utf8mb4",

            "CREATE TABLE IF NOT EXISTS PunishmentHistory (" +
            "id INTEGER IDENTITY PRIMARY KEY, " +
            "name VARCHAR(16), " +
            "uuid VARCHAR(32), " +
            "reason VARCHAR(100), " +
            "operator VARCHAR(16), " +
            "punishmentType VARCHAR(16), " +
            "start BIGINT, " +
            "end BIGINT, " +
            "calculation VARCHAR(50))"
    ),
    INSERT_PUNISHMENT(
            "INSERT INTO `Punishments` " +
            "(`name`, `uuid`, `reason`, `operator`, `punishmentType`, `start`, `end`, `calculation`) " +
            "VALUES (?, ?, ?, ?, ?, FROM_UNIXTIME(? * 0.001), FROM_UNIXTIME(? * 0.001), ?)",

            "INSERT INTO Punishments " +
            "(name, uuid, reason, operator, punishmentType, start, end, calculation) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
    ),
    INSERT_PUNISHMENT_WITH_ID(
            "INSERT INTO `Punishments` " +
            "(`id`, `name`, `uuid`, `reason`, `operator`, `punishmentType`, `start`, `end`, `calculation`) " +
            "VALUES (?, ?, ?, ?, ?, ?, FROM_UNIXTIME(? * 0.001), FROM_UNIXTIME(? * 0.001), ?)",

            ""
    ),
    BUMP_PUNISHMENT_AUTO_ID(
            "INSERT INTO `Punishments` (`id`) VALUES (NULL);\n" + 
            "DELETE FROM `Punishments` WHERE `id` = LAST_INSERT_ID()",

            "INSERT INTO Punishments (id) VALUES (NULL);\n" + 
            "DELETE FROM Punishments WHERE id = IDENTITY()"
    ),
    INSERT_PUNISHMENT_HISTORY(
            "INSERT INTO `PunishmentHistory` " +
            "(`name`, `uuid`, `reason`, `operator`, `punishmentType`, `start`, `end`, `calculation`) " +
            "VALUES (?, ?, ?, ?, ?, FROM_UNIXTIME(? * 0.001), FROM_UNIXTIME(? * 0.001), ?)",

            "INSERT INTO PunishmentHistory " +
            "(name, uuid, reason, operator, punishmentType, start, end, calculation) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
    ),
    INSERT_PUNISHMENT_HISTORY_WITH_ID(
            "INSERT INTO `PunishmentHistory` " +
            "(`id`, `name`, `uuid`, `reason`, `operator`, `punishmentType`, `start`, `end`, `calculation`) " +
            "VALUES (?, ?, ?, ?, ?, ?, FROM_UNIXTIME(? * 0.001), FROM_UNIXTIME(? * 0.001), ?)",

            ""
    ),
    SELECT_EXACT_PUNISHMENT(
            "SELECT * FROM `Punishments` WHERE `uuid` = ? AND `start` = FROM_UNIXTIME(? * 0.001)",
            "SELECT * FROM Punishments WHERE uuid = ? AND start = ?"
    ),
    DELETE_PUNISHMENT(
            "DELETE FROM `Punishments` WHERE `id` = ?",
            "DELETE FROM Punishments WHERE id = ?"
    ),
    DELETE_OLD_PUNISHMENTS(
            "DELETE FROM `Punishments` WHERE `end` <= FROM_UNIXTIME(? * 0.001) AND `end` IS NOT NULL",
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
    ),

    SELECT_NEXT_AUTO_ID(
            "SELECT `AUTO_INCREMENT` FROM `information_schema`.`TABLES` " + 
            "WHERE `TABLE_SCHEMA` = DATABASE() AND (`TABLE_NAME` = 'Punishments' OR `TABLE_NAME` = 'PunishmentHistory') ORDER BY `AUTO_INCREMENT` DESC LIMIT 1",
            ""
    ),
    SET_PUNISHMENT_AUTO_ID(
            "ALTER TABLE `Punishments` AUTO_INCREMENT = ?",
            ""
    ),
    SET_PUNISHMENT_HISTORY_AUTO_ID(
            "ALTER TABLE `PunishmentHistory` AUTO_INCREMENT = ?",
            ""
    ),

    DETECT_PUNISHMENT_MIGRATION_STATUS(
            "SELECT `DATA_TYPE` FROM `information_schema`.`COLUMNS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'Punishments' AND `COLUMN_NAME` = 'punishmentType'",
            ""
    ),
    DETECT_PUNISHMENT_HISTORY_MIGRATION_STATUS(
            "SELECT `DATA_TYPE` FROM `information_schema`.`COLUMNS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'PunishmentHistory' AND `COLUMN_NAME` = 'punishmentType'",
            ""
    ),

    MIGRATE_PUNISHMENT(
            "ALTER TABLE `Punishments` " + 
            "CHANGE `name` `name` VARCHAR(16) CHARACTER SET utf8mb4 NOT NULL, " + 
            "CHANGE `uuid` `uuid` VARCHAR(32) CHARACTER SET utf8mb4 NOT NULL, " + 
            "CHANGE `reason` `reason` TEXT CHARACTER SET utf8mb4 NOT NULL, " + 
            "CHANGE `operator` `operator` VARCHAR(16) CHARACTER SET utf8mb4 NOT NULL, " + 
            "CHANGE `punishmentType` `punishmentType` " + PunishmentType.getAsMysqlEnum() + " NOT NULL, " + 
            "CHANGE `start` `start_old` BIGINT NOT NULL, " + 
            "CHANGE `end` `end_old` BIGINT NOT NULL, " + 
            "CHANGE `calculation` `calculation` TINYTEXT CHARACTER SET utf8mb4 NULL DEFAULT NULL, " + 
            "ADD `start` TIMESTAMP(3) NOT NULL AFTER `start_old`, " + 
            "ADD `end` TIMESTAMP(3) NULL DEFAULT NULL AFTER `end_old`, " +
            "ADD INDEX (`uuid`), " +
            "ADD INDEX (`start`);\n" +

            "UPDATE `Punishments` SET " + 
            "`start` = FROM_UNIXTIME(`start_old` * 0.001), " + 
            "`end` = FROM_UNIXTIME(`end_old` * 0.001);\n" + 

            "ALTER TABLE `Punishments` " + 
            "DROP `start_old`, " + 
            "DROP `end_old`;",

            ""
    ),
    MIGRATE_PUNISHMENT_HISTORY(
            "ALTER TABLE `PunishmentHistory` " + 
            "CHANGE `name` `name` VARCHAR(16) CHARACTER SET utf8mb4 NOT NULL, " + 
            "CHANGE `uuid` `uuid` VARCHAR(32) CHARACTER SET utf8mb4 NOT NULL, " + 
            "CHANGE `reason` `reason` TEXT CHARACTER SET utf8mb4 NOT NULL, " + 
            "CHANGE `operator` `operator` VARCHAR(16) CHARACTER SET utf8mb4 NOT NULL, " + 
            "CHANGE `punishmentType` `punishmentType` " + PunishmentType.getAsMysqlEnum() + " NOT NULL,"  + 
            "CHANGE `start` `start_old` BIGINT NOT NULL, " + 
            "CHANGE `end` `end_old` BIGINT NOT NULL, " + 
            "CHANGE `calculation` `calculation` TINYTEXT CHARACTER SET utf8mb4 NULL DEFAULT NULL, " + 
            "ADD `start` TIMESTAMP(3) NOT NULL AFTER `start_old`, " + 
            "ADD `end` TIMESTAMP(3) NULL DEFAULT NULL AFTER `end_old`, " +
            "ADD INDEX (`uuid`), " +
            "ADD INDEX (`start`);\n" +

            "UPDATE `PunishmentHistory` SET " + 
            "`start` = FROM_UNIXTIME(`start_old` * 0.001), " + 
            "`end` = FROM_UNIXTIME(`end_old` * 0.001);\n" + 

            "ALTER TABLE `PunishmentHistory` " + 
            "DROP `start_old`, " + 
            "DROP `end_old`;",

            ""
    );

    private static final String mysqlAsterix =
            "`id`, `name`, `uuid`, `reason`, `operator`, `punishmentType`, CONVERT(UNIX_TIMESTAMP(`start`) * 1000, SIGNED INTEGER) AS `start`, CONVERT(IFNULL(UNIX_TIMESTAMP(`end`) * 1000, -1), SIGNED INTEGER) AS `end`, `calculation`";

    private String mysql;
    private String hsqldb;

    SQLQuery(String mysql, String hsqldb) {
        this.mysql = mysql.replace("SELECT *", "SELECT " + mysqlAsterix);
        this.hsqldb = hsqldb;
    }

    @Override
    public String toString() {
        return DatabaseManager.get().isUseMySQL() ? mysql : hsqldb;
    }

    public String getMysql() {
        return mysql;
    }

    public String getHsqldb() {
        return hsqldb;
    }
}