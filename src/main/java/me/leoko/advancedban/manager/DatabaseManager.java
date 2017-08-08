package me.leoko.advancedban.manager;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.utils.SQLQuery;

import java.io.File;
import java.io.IOException;
import java.sql.*;

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

    public void setup(boolean useMySQLServer){
        MethodInterface mi = Universal.get().getMethods();

        if(useMySQLServer){
            File file = new File(mi.getDataFolder(), "MySQL.yml");
            boolean createFile = !file.exists();

            if (createFile) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mi.loadMySQLFile(file);

            if (createFile) {
                mi.createMySQLFile(file);
                failedMySQL = true;
            }else{
                ip = mi.getString(mi.getMySQLFile(), "MySQL.IP", "Unknown");
                dbName = mi.getString(mi.getMySQLFile(), "MySQL.DB-Name", "Unknown");
                usrName = mi.getString(mi.getMySQLFile(), "MySQL.Username", "Unknown");
                password = mi.getString(mi.getMySQLFile(), "MySQL.Password", "Unknown");
                port = mi.getInteger(mi.getMySQLFile(), "MySQL.Port", 3306);

                connectMySQLServer();
            }
        }

        useMySQL = useMySQLServer && !failedMySQL;

        if(!useMySQL){
            try {
                Class.forName("org.hsqldb.jdbc.JDBCDriver" );
            } catch (Exception e) {
                System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
                e.printStackTrace();
                return;
            }

            try {
                connection = DriverManager.getConnection("jdbc:hsqldb:file:"+mi.getDataFolder().getPath()+"\\data\\storage;hsqldb.lock_file=false", "SA", "");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        executeStatement(SQLQuery.CREATE_TABLE_PUNISHMENT);
        executeStatement(SQLQuery.CREATE_TABLE_PUNISHMENT_HISTORY);
    }

    public void shutdown(){
        try {
            if(!useMySQL)
                connection.prepareStatement("SHUTDOWN").execute();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void connectMySQLServer() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + dbName + "?verifyServerCertificate=false&useSSL=false&autoReconnect=true", usrName, password);
        } catch (Exception exc) {
            System.out.println("AdvancedBan <> \n \n \nMySQL-Error\nCould not connect to MySQL-Server!\nDisabling plugin!\nCheck your MySQL.yml \nSkype: Leoko33 \n \n");
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
        try {
            PreparedStatement statement = connection.prepareStatement(sql.toString());

            for (int i = 0; i < parameters.length; i++) {
                Object obj = parameters[i];
                if(obj instanceof Integer)
                    statement.setInt(i + 1, (Integer) obj);
                else if(obj instanceof String)
                    statement.setString(i+1, (String) obj);
                else if(obj instanceof Long)
                    statement.setLong(i+1, (Long) obj);
                else
                    statement.setObject(i+1, obj);
            }

            if(result){
                ResultSet resultSet = statement.executeQuery();
                statement.close();
                return resultSet;
            }else{
                statement.execute();
                statement.close();
            }
            return null;
        } catch (SQLException e) {
            //TODO change here and below...
//            System.out.println("AdvancedBan <> Failed due to exception: " + e.getMessage());
            System.out.println("SQL -> "+sql);
            e.printStackTrace();
            return null;
        }
    }

    public boolean isConnectionValid(int timeout){
        try {
            return connection.isValid(timeout);
        } catch (SQLException e) {
            e.printStackTrace();
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
