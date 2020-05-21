package me.leoko.advancedban.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;

public class DynamicDataSource {
    private HikariConfig config = new HikariConfig();

    public DynamicDataSource(boolean preferMySQL) throws ClassNotFoundException {
        MethodInterface mi = Universal.get().getMethods();
        if (preferMySQL) {
            String ip = mi.getString(mi.getMySQLFile(), "MySQL.IP", "Unknown");
            String dbName = mi.getString(mi.getMySQLFile(), "MySQL.DB-Name", "Unknown");
            String usrName = mi.getString(mi.getMySQLFile(), "MySQL.Username", "Unknown");
            String password = mi.getString(mi.getMySQLFile(), "MySQL.Password", "Unknown");
            int port = mi.getInteger(mi.getMySQLFile(), "MySQL.Port", 3306);

            Class.forName("com.mysql.jdbc.Driver");
            config.setJdbcUrl("jdbc:mysql://" + ip + ":" + port + "/" + dbName + "?verifyServerCertificate=false&useSSL=false&useUnicode=true&characterEncoding=utf8");
            config.setUsername(usrName);
            config.setPassword(password);
        } else {
            // No need to worry about relocation because the maven-shade-plugin also changes strings
            String driverClassName = "org.hsqldb.jdbc.JDBCDriver";
            Class.forName(driverClassName);
            config.setDriverClassName(driverClassName);
            config.setJdbcUrl("jdbc:hsqldb:file:" + mi.getDataFolder().getPath() + "/data/storage;hsqldb.lock_file=false");
            config.setUsername("SA");
            config.setPassword("");
        }
    }

    public HikariDataSource generateDataSource(){
        return new HikariDataSource(config);
    }
}
