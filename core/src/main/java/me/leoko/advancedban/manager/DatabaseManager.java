package me.leoko.advancedban.manager;

import com.zaxxer.hikari.HikariDataSource;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.utils.DynamicDataSource;
import me.leoko.advancedban.utils.SQLQuery;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The Database Manager is used to interact directly with the database is use.<br>
 * Will automatically direct the requests to either MySQL or HSQLDB.
 * <br><br>
 * Looking to request {@link me.leoko.advancedban.utils.Punishment Punishments} from the Database?
 * Use {@link PunishmentManager#getPunishments(SQLQuery, Object...)} or
 * {@link PunishmentManager#getPunishmentFromResultSet(ResultSet)} for already parsed data.
 */
public class DatabaseManager {

    private HikariDataSource dataSource;
    private boolean useMySQL;

    private RowSetFactory factory;
    
    private static DatabaseManager instance = null;

    /**
     * Get the instance of the command manager
     *
     * @return the database manager instance
     */
    public static synchronized DatabaseManager get() {
        return instance == null ? instance = new DatabaseManager() : instance;
    }

    /**
     * Initially connects to the database and sets up the required tables of they don't already exist.
     *
     * @param useMySQLServer whether to preferably use MySQL (uses HSQLDB as fallback)
     */
    public void setup(boolean useMySQLServer) {
        useMySQL = useMySQLServer;

        dataSource = new DynamicDataSource(useMySQL).generateDataSource();

        executeStatement(SQLQuery.CREATE_TABLE_PUNISHMENT);
        executeStatement(SQLQuery.CREATE_TABLE_PUNISHMENT_HISTORY);
    }

    /**
     * Shuts down the HSQLDB if used.
     */
    public void shutdown() {
        if (!useMySQL) {
            try(Connection connection = dataSource.getConnection(); final PreparedStatement statement = connection.prepareStatement("SHUTDOWN")){
                statement.execute();
            }catch (SQLException | NullPointerException exc){
                Universal.get().log("An unexpected error has occurred turning off the database");
                Universal.get().debugException(exc);
            }
        }

        dataSource.close();
    }
    
    private CachedRowSet createCachedRowSet() throws SQLException {
    	if (factory == null) {
    		factory = RowSetProvider.newFactory();
    	}
    	return factory.createCachedRowSet();
    }

    /**
     * Execute a sql statement without any results.
     *
     * @param sql        the sql statement
     * @param parameters the parameters
     */
    public void executeStatement(SQLQuery sql, Object... parameters) {
        executeStatement(sql, false, parameters);
    }

    /**
     * Execute a sql statement.
     *
     * @param sql        the sql statement
     * @param parameters the parameters
     * @return the result set
     */
    public ResultSet executeResultStatement(SQLQuery sql, Object... parameters) {
        return executeStatement(sql, true, parameters);
    }

    private ResultSet executeStatement(SQLQuery sql, boolean result, Object... parameters) {
        return executeStatement(sql.toString(), result, parameters);
    }

    private synchronized ResultSet executeStatement(String sql, boolean result, Object... parameters) {
    	try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

    		for (int i = 0; i < parameters.length; i++) {
    			statement.setObject(i + 1, parameters[i]);
    		}

    		if (result) {
    			CachedRowSet results = createCachedRowSet();
    			results.populate(statement.executeQuery());
    			return results;
    		}
   			statement.execute();
    	} catch (SQLException ex) {
    		Universal.get().log(
   					"An unexpected error has occurred executing an Statement in the database\n"
   							+ "Please check the plugins/AdvancedBan/logs/latest.log file and report this "
    						+ "error in: https://github.com/DevLeoko/AdvancedBan/issues"
    				);
    		Universal.get().debug("Query: \n" + sql);
    		Universal.get().debugSqlException(ex);
       	} catch (NullPointerException ex) {
            Universal.get().log(
                    "An unexpected error has occurred connecting to the database\n"
                            + "Check if your MySQL data is correct and if your MySQL-Server is online\n"
                            + "Please check the plugins/AdvancedBan/logs/latest.log file and report this "
                            + "error in: https://github.com/DevLeoko/AdvancedBan/issues"
            );
            Universal.get().debugException(ex);
        }
        return null;
    }

    /**
     * Check whether there is a valid connection to the database.
     *
     * @return whether there is a valid connection
     */
    public boolean isConnectionValid() {
        return dataSource.isRunning();
    }

    /**
     * Check whether MySQL is actually used.
     *
     * @return whether MySQL is used
     */
    public boolean isUseMySQL() {
        return useMySQL;
    }
}