package me.leoko.advancedban.manager;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.utils.SQLQuery;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {

    private String ip;
    private String dbName;
    private String usrName;
    private String password;
    private int port = 3306;
    private Connection connection;
    private boolean failedMySQL = false;
    private boolean useMySQL;

    private static DatabaseManager instance = null;

    public static DatabaseManager get() {
        return instance == null ? instance = new DatabaseManager() : instance;
    }

    public void setup(boolean useMySQLServer) {
        MethodInterface mi = Universal.get().getMethods();

        if (useMySQLServer) {
            File file = new File(mi.getDataFolder(), "MySQL.yml");
            boolean createFile = !file.exists();

            if (createFile) {
                try {
                    file.createNewFile();
                } catch (IOException ex) {
                    Universal.get().log("§cAn unexpected error has occurred while creating the MySQL.yml file, try restarting the server.");
                    Universal.get().debug(ex.getMessage());
                }
            }
            mi.loadMySQLFile(file);

            if (createFile) {
                mi.createMySQLFile(file);
                failedMySQL = true;
            } else {
                ip = mi.getString(mi.getMySQLFile(), "MySQL.IP", "Unknown");
                dbName = mi.getString(mi.getMySQLFile(), "MySQL.DB-Name", "Unknown");
                usrName = mi.getString(mi.getMySQLFile(), "MySQL.Username", "Unknown");
                password = mi.getString(mi.getMySQLFile(), "MySQL.Password", "Unknown");
                port = mi.getInteger(mi.getMySQLFile(), "MySQL.Port", 3306);

                connectMySQLServer();
            }
        }

        useMySQL = useMySQLServer && !failedMySQL;

        if (useMySQL) {
            try {
                migrateIfNeccessary(SQLQuery.DETECT_PUNISHMENT_MIGRATION_STATUS, SQLQuery.MIGRATE_PUNISHMENT);
                migrateIfNeccessary(SQLQuery.DETECT_PUNISHMENT_HISTORY_MIGRATION_STATUS, SQLQuery.MIGRATE_PUNISHMENT_HISTORY);
            } catch (SQLException ex) {
                Universal.get().log(
                        " \n"
                        + " MySQL-Error\n"
                        + " Could not migrate old tables!\n"
                        + " Skype: Leoko33\n"
                        + " Issue tracker: https://github.com/DevLeoko/AdvancedBan/issues\n"
                        + " \n"
                );
                Universal.get().debug(ex);
            }
        } else {
            connection = connectHSQL();
        }

        executeStatement(SQLQuery.CREATE_TABLE_PUNISHMENT);
        executeStatement(SQLQuery.CREATE_TABLE_PUNISHMENT_HISTORY);

        if (useMySQL) {
            try {
                migrateHSQL();
            } catch (SQLException ex) {
                Universal.get().log(
                        " \n"
                        + " MySQL-Error\n"
                        + " Could not migrate HSQLDB!\n"
                        + " Skype: Leoko33\n"
                        + " Issue tracker: https://github.com/DevLeoko/AdvancedBan/issues\n"
                        + " \n"
                );
                Universal.get().debug(ex);
            }

            syncAutoId();
        }
    }

    public void shutdown() {
        try {
            if (!useMySQL) {
                connection.prepareStatement("SHUTDOWN").execute();
                connection.close();
            }
        } catch (SQLException ex) {
            Universal.get().log("An unexpected error has occurred turning off the database");
            Universal.get().debug(ex);
        }
    }

    private void connectMySQLServer() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + dbName + "?verifyServerCertificate=false&useSSL=false&autoReconnect=true&useUnicode=true&characterEncoding=utf8&allowMultiQueries=true", usrName, password);
        } catch (SQLException exc) {
            Universal.get().log(
                    " \n"
                    + " MySQL-Error\n"
                    + " Could not connect to MySQL-Server!\n"
                    + " Disabling plugin!\n"
                    + " Check your MySQL.yml\n"
                    + " Skype: Leoko33\n"
                    + " Issue tracker: https://github.com/DevLeoko/AdvancedBan/issues \n"
                    + " \n"
            );
            failedMySQL = true;
        }
    }

    private Connection connectHSQL() {
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (ClassNotFoundException ex) {
            Universal.get().log("§cERROR: failed to load HSQLDB JDBC driver.");
            Universal.get().debug(ex.getMessage());

            return null;
        }

        try {
            return DriverManager.getConnection("jdbc:hsqldb:file:" + Universal.get().getMethods().getDataFolder().getPath() + "/data/storage" + ";hsqldb.lock_file=false", "SA", "");
        } catch (SQLException ex) {
            Universal.get().log(
                    " \n"
                    + " HSQLDB-Error\n"
                    + " Could not connect to HSQLDB-Server!\n"
                    + " Disabling plugin!\n"
                    + " Skype: Leoko33\n"
                    + " Issue tracker: https://github.com/DevLeoko/AdvancedBan/issues\n"
                    + " \n"
            );
            Universal.get().debug(ex);
        }

        return null;
    }

    public void executeStatement(SQLQuery sql, Object... parameters) {
        executeStatement(sql, false, parameters);
    }

    public void executeMultipleStatements(SQLQuery sql) {
        final String fullQuery = sql.toString();
        final String[] queries = useMySQL? new String[] {fullQuery} : fullQuery.split(";\n");

        for(final String query : queries) {
            executeStatement(query, false);
        }
    }

    public ResultSet executeResultStatement(SQLQuery sql, Object... parameters) {
        return executeStatement(sql, true, parameters);
    }

    private ResultSet executeStatement(SQLQuery sql, boolean result, Object... parameters) {
        return executeStatement(sql.toString(), result, parameters);
    }

    public ResultSet executeStatement(String sql, boolean result, Object... parameters) {
        return executeStatement(connection, sql, result, parameters);
    }

    private ResultSet executeStatement(Connection connection, String sql, boolean result, Object... parameters) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);

            for (int i = 0; i < parameters.length; i++) {
                Object obj = parameters[i];
                if (obj instanceof Integer) {
                    statement.setInt(i + 1, (Integer) obj);
                } else if (obj instanceof String) {
                    statement.setString(i + 1, (String) obj);
                } else if (obj instanceof Long) {
                    statement.setLong(i + 1, (Long) obj);
                } else {
                    statement.setObject(i + 1, obj);
                }
            }

            if (result) {
                ResultSet resultSet = statement.executeQuery();
                return resultSet;
            } else {
                statement.execute();
                statement.close();
            }
            return null;
        } catch (SQLException ex) {
            Universal.get().log(
                    "An unexpected error has ocurred executing an Statement in the database\n"
                    + "Please check the plugins/AdvancedBan/logs/latest.log file and report this"
                    + "error in: https://github.com/DevLeoko/AdvancedBan/issues"
            );
            Universal.get().debug("Query: \n" + sql);
            Universal.get().debug(ex);
            return null;
        }
    }

    public boolean isConnectionValid(int timeout) {
        try {
            return connection.isValid(timeout);
        } catch (SQLException ex) {
            Universal.get().log("An unexpected error has occurred with the database.");
            Universal.get().debug(ex);
            return false;
        }
    }

    public boolean isFailedMySQL() {
        return failedMySQL;
    }

    public boolean isUseMySQL() {
        return useMySQL;
    }

    private void migrateIfNeccessary(SQLQuery detectionQuery, SQLQuery migrationQuery) throws SQLException {
        try (final ResultSet result = executeResultStatement(detectionQuery)) {
            if (!result.next()) return;

            if ("varchar".equalsIgnoreCase(result.getString("DATA_TYPE"))) {
                executeMultipleStatements(migrationQuery);
            }
        }
    }

    private int getNextAutoId() {
        try (final ResultSet result = executeResultStatement(SQLQuery.SELECT_NEXT_AUTO_ID)) {
            if (result.next()) {
                return result.getInt(1);
            }
        } catch (SQLException ex) {
            Universal.get().log(
                    "An unexpected error has ocurred while trying to retrieve the highest id\n"
                    + "Please check the plugins/AdvancedBan/logs/latest.log file and report this"
                    + "error in: https://github.com/DevLeoko/AdvancedBan/issues"
            );
            Universal.get().debug(ex);
        }
        
        return useMySQL ? 1 : 0;
    }

    private void syncAutoId() {
        final int nextId = getNextAutoId();

        syncAutoId(nextId);
    }

    private void syncAutoId(int nextId) {
        executeStatement(SQLQuery.SET_PUNISHMENT_AUTO_ID.toString(), false, nextId);
        executeStatement(SQLQuery.SET_PUNISHMENT_HISTORY_AUTO_ID.toString(), false, nextId);
    }

    private void migrateHSQL() throws SQLException {
        final File dataDir = Universal.get().getMethods().getDataFolder();
        final File hsqlScript = new File(dataDir, "/data/storage.script");

        if (!hsqlScript.exists()) return;

        Universal.get().log("Starting migration from HSQLDB to MySQL...");

        final int idOffset = getNextAutoId();
        int id;
        int maxId = 0;

        try {
            try (final Connection hsqlConnection = connectHSQL()) {
                try (final ResultSet result = executeStatement(hsqlConnection, SQLQuery.SELECT_ALL_PUNISHMENTS_HISTORY.getHsqldb(), true)) {
                    while(result.next()) {
                        id = result.getInt("id") + idOffset;
                        executeStatement(
                                SQLQuery.INSERT_PUNISHMENT_HISTORY_WITH_ID,
                                id,
                                result.getString("name"),
                                result.getString("uuid").replace("-", ""),
                                result.getString("reason"),
                                result.getString("operator"),
                                result.getString("punishmentType"),
                                result.getLong("start"),
                                result.getLong("end"),
                                result.getString("calculation")
                        );

                        if (id > maxId)
                            maxId = id;
                    }
                }

                try (final ResultSet result = executeStatement(hsqlConnection, SQLQuery.SELECT_ALL_PUNISHMENTS.getHsqldb(), true)) {
                    while(result.next()) {
                        id = result.getInt("id") + idOffset;
                        executeStatement(
                                SQLQuery.INSERT_PUNISHMENT_WITH_ID,
                                id,
                                result.getString("name"),
                                result.getString("uuid").replace("-", ""),
                                result.getString("reason"),
                                result.getString("operator"),
                                result.getString("punishmentType"),
                                result.getLong("start"),
                                result.getLong("end"),
                                result.getString("calculation")
                        );

                        if (id > maxId)
                            maxId = id;
                    }
                }

                executeStatement(hsqlConnection, "SHUTDOWN", false);
            }

            syncAutoId(idOffset + maxId + 1);

            Files.move(hsqlScript.getParentFile().toPath(), new File(dataDir, "/data.old").toPath());
        } catch (Exception e) {
            throw new SQLException("Migration failed: " + e.getMessage(), e);
        }

        Universal.get().log("Migration completed!");
    }
}