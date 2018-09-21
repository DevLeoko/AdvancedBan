package me.leoko.advancedban.manager;

import lombok.RequiredArgsConstructor;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanManager;
import me.leoko.advancedban.configuration.MySQLConfiguration;
import me.leoko.advancedban.utils.SQLQuery;

import java.sql.*;
import java.util.Optional;

@RequiredArgsConstructor
public class DatabaseManager implements AdvancedBanManager {
    private final AdvancedBan advancedBan;
    private MySQLConfiguration.MySQL configuration;
    private Connection connection;
    private boolean failedMySQL = false;
    private boolean useMySQL;

    public void onEnable() {
        Optional<MySQLConfiguration> config = advancedBan.getMySQLConfiguration();
        config.ifPresent(mySQLConfiguration -> {
            this.configuration = mySQLConfiguration.getMySQL();
            connectMySQLServer();
        });

        useMySQL = config.isPresent() && !failedMySQL;

        if (!useMySQL) {
            try {
                Class.forName("org.hsqldb.jdbc.JDBCDriver");
            } catch (ClassNotFoundException ex) {
                advancedBan.getLogger().info("§cERROR: failed to load HSQLDB JDBC driver.");
                advancedBan.getLogger().logException(ex);
                return;
            }
            try {
                connection = DriverManager.getConnection("jdbc:hsqldb:file:" + advancedBan.getDataFolderPath() +
                        "/data/storage;hsqldb.lock_file=false", "SA", "");
            } catch (SQLException ex) {
                advancedBan.getLogger().info("Could not connect to HSQLDB-Server!");
            }
        }

        executeStatement(SQLQuery.CREATE_TABLE_PUNISHMENT);
        executeStatement(SQLQuery.CREATE_TABLE_PUNISHMENT_HISTORY);
    }

    public void onDisable() {
        if (!useMySQL) {
            try (PreparedStatement statement = connection.prepareStatement("SHUTDOWN")) {
                statement.execute();
            } catch (SQLException ex) {
                advancedBan.getLogger().warn("An unexpected error has occurred turning off the database");
                advancedBan.getLogger().logException(ex);
            }
        }
        try {
            connection.close();
        } catch (SQLException e) {
            advancedBan.getLogger().warn(
                    "Unable to close database connection\n" +
                            "Check logs for more info");
            advancedBan.getLogger().logException(e);
        }
    }

    private void connectMySQLServer() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + configuration.getAddress() + ":" +
                            configuration.getPort() + "/" + configuration.getDatabaseName() +
                            "?verifyServerCertificate=false&useSSL=false&autoReconnect=true&useUnicode=true&characterEncoding=utf8",
                    configuration.getUsername(), configuration.getPassword());
        } catch (SQLException exc) {
            advancedBan.getLogger().warn("Could not connect to MySQL-Server!");
            failedMySQL = true;
        }
    }

    public void executeStatement(SQLQuery sql, Object... parameters) {
        executeStatement(sql, false, parameters);
    }

    public ResultSet executeResultStatement(SQLQuery sql, Object... parameters) {
        return executeStatement(sql, true, parameters);
    }

    private ResultSet executeStatement(SQLQuery sql, boolean result, Object... parameters) {
        return executeStatement(useMySQL ? sql.getMysql() : sql.getHsqldb(), result, parameters);
    }

    public ResultSet executeStatement(String sql, boolean result, Object... parameters) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                Object param = parameters[i];
                if (param instanceof Integer) {
                    statement.setInt(i + 1, (Integer) param);
                } else if (param instanceof String) {
                    statement.setString(i + 1, (String) param);
                } else if (param instanceof Long) {
                    statement.setLong(i + 1, (Long) param);
                } else {
                    statement.setObject(i + 1, param);
                }
            }

            if (result) {
                return statement.executeQuery();
            } else {
                statement.execute();
            }
            return null;
        } catch (SQLException ex) {
            advancedBan.getLogger().warn(
                    "An unexpected error has occurred executing an Statement in the database\n"
                            + "Please check the plugins/AdvancedBan/logs/latest.log file and report this\n"
                    + "error in: https://github.com/DevLeoko/AdvancedBan/issues"
            );
            advancedBan.getLogger().debug("Query: \n" + sql);
            advancedBan.getLogger().logException(ex);
            return null;
        }
    }

    public boolean isConnectionValid(int timeout) {
        try {
            return connection.isValid(timeout);
        } catch (SQLException ex) {
            advancedBan.getLogger().warn("An unexpected error has occurred with the database.");
            advancedBan.getLogger().logException(ex);
            return false;
        }
    }

    public boolean isFailedMySQL() {
        return failedMySQL;
    }

    public boolean isUseMySQL() {
        return useMySQL;
    }
}