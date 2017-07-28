package me.leoko.advancedban.manager;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLManager {
    private final String ip;
    private final String dbName;
    private final String usrName;
    private final String password;
    private final boolean autoRefresh;
    private final int refreshMin;
    private int port = 3306;
    private Connection connection;
    private boolean failed = false;

    public MySQLManager(String ip, String dbName, String usrName, String password, boolean autoRefresh, int refreshMin) {
        this.ip = ip;
        this.dbName = dbName;
        this.usrName = usrName;
        this.password = password;
        this.autoRefresh = autoRefresh;
        this.refreshMin = refreshMin;

        connect();
    }

    public MySQLManager(File f, boolean autoRefresh, int refreshMin) {
        boolean createFile = !f.exists();

        if (createFile) {
            try {
                //noinspection ResultOfMethodCallIgnored
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        MethodInterface mi = Universal.get().getMethods();
        mi.loadMySQLFile(f);

        if (createFile) {
            mi.createMySQLFile(f);
            failed = true;
        }

        ip = mi.getString(mi.getMySQLFile(), "MySQL.IP", "Unknown");
        dbName = mi.getString(mi.getMySQLFile(), "MySQL.DB-Name", "Unknown");
        usrName = mi.getString(mi.getMySQLFile(), "MySQL.Username", "Unknown");
        password = mi.getString(mi.getMySQLFile(), "MySQL.Password", "Unknown");
        port = mi.getInteger(mi.getMySQLFile(), "MySQL.Port", 3306);
        this.autoRefresh = autoRefresh;
        this.refreshMin = refreshMin;

        connect();
    }

    private boolean hasFailed() {
        if (failed) {
            System.out.println("AdvancedBan <> Skipped executing SQL because there is no connection to the MySQL-Server please restart the server!");
        }
        return failed;
    }

    private void reconnect() {
        reconnect(0);
    }

    private void reconnect(int i) {
        try {
            connection.close();
            connection = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + dbName + "?autoReconnect=true", usrName, password);
        } catch (SQLException exc) {
            i++;
            System.out.println("AdvancedBan <> Failed to reconnect! [" + i + "/10]");
            if (i < 10) {
                System.out.println("AdvancedBan <> Trying to reconnect to MySQL-Server...");
                reconnect(i);
            } else {
                System.out.println("AdvancedBan <> Giving up to reconnect to the MySQL-Server due to " + exc.getMessage());
                failed = true;
            }
        }
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + dbName + "?verifyServerCertificate=false&useSSL=false", usrName, password);
        } catch (Exception exc) {
            System.out.println("AdvancedBan <> \n \n \nMySQL-Error\nCould not connect to MySQL-Server!\nDisabling plugin!\nCheck your MySQL.yml \nSkype: Leoko33 \n \n");
            failed = true;
            return;
        }

        if (autoRefresh) {
            Universal.get().getMethods().scheduleAsyncRep(this::reconnect, 20 * 60 * refreshMin, 20 * 60 * refreshMin);
        }
    }

    public boolean isFailed() {
        return failed;
    }

    public void checkDB(String db, String createSQL) throws SQLException {
        if (hasFailed()) {
            return;
        }
        DatabaseMetaData md = connection.getMetaData();
        ResultSet rs = md.getTables(null, null, db, null);

        if (!rs.next()) {
            Statement Stmt = connection.createStatement();
            Stmt.executeUpdate(createSQL);
        }
    }

    public void executeStatement(String sql) {
        if (hasFailed()) {
            return;
        }
        try {
            connection.prepareStatement(sql).execute();
        } catch (SQLException e) {
            System.out.println("AdvancedBan <> Failed due to exception: " + e.getMessage());
            reconnect();
            executeStatement(sql);
        }
    }

    public ResultSet executeRespStatement(String sql) {
        if (hasFailed()) {
            return null;
        }
        try {
            return connection.prepareStatement(sql).executeQuery();
        } catch (SQLException e) {
            reconnect();
            executeStatement(sql);
        }
        return null;
    }

    public Connection getConnection() {
        return connection;
    }
}
