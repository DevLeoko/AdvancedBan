package me.leoko.advancedban.manager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.utils.SQLQuery;

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

    public void setup(boolean useMySQLServer, boolean useMariaDBDriver) {
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

                connectMySQLServer(useMariaDBDriver);
            }
        }

        useMySQL = useMySQLServer && !failedMySQL;

        if (!useMySQL) {
            try {
                Class.forName("org.hsqldb.jdbc.JDBCDriver");
            } catch (ClassNotFoundException ex) {
                Universal.get().log("§cERROR: failed to load HSQLDB JDBC driver.");
                Universal.get().debug(ex.getMessage());
                return;
            }
            try {
                connection = DriverManager.getConnection("jdbc:hsqldb:file:" + mi.getDataFolder().getPath() + "/data/storage;hsqldb.lock_file=false", "SA", "");
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
            }
        }

        executeStatement(SQLQuery.CREATE_TABLE_PUNISHMENT);
        executeStatement(SQLQuery.CREATE_TABLE_PUNISHMENT_HISTORY);
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

    private void connectMySQLServer(boolean useMariaDBDriver) {
        if (useMariaDBDriver) {
            try {
                connection = DriverManager.getConnection("jdbc:mariadb://" + ip + ":" + port + "/" + dbName + "?verifyServerCertificate=false&useSSL=false&autoReconnect=true", usrName, password);
            } catch (SQLException exc) {
                Universal.get().log(
                        " \n"
                        + " MariaDB-Error\n"
                        + " Could not connect to MySQL-Server!\n"
                        + " Disabling plugin!\n"
                        + " Check your MySQL.yml\n"
                        + " Skype: Leoko33\n"
                        + " Issue tracker: https://github.com/DevLeoko/AdvancedBan/issues \n"
                        + " \n"
                );
                failedMySQL = true;
            }
        } else {
            try {
                connection = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + dbName + "?verifyServerCertificate=false&useSSL=false&autoReconnect=true&disableMariaDbDriver", usrName, password);
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
    }

    public void executeStatement(SQLQuery sql, Object... parameters) {
        executeStatement(sql, false, parameters);
    }

    public ResultSet executeResultStatement(SQLQuery sql, Object... parameters) {
        return executeStatement(sql, true, parameters);
    }

    private ResultSet executeStatement(SQLQuery sql, boolean result, Object... parameters) {
        return executeStatement(sql.toString(), result, parameters);
    }

    public ResultSet executeStatement(String sql, boolean result, Object... parameters) {
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
            //TODO change here and below...
//            System.out.println("AdvancedBan <> Failed due to exception: " + e.getMessage());
            Universal.get().log(
                    "An unexpected error has ocurred executing an Statement in the database\n"
                    + "Please check the debug.log file in the plugin's folder and report this"
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
}
