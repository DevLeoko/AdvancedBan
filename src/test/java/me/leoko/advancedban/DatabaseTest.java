package me.leoko.advancedban;

import me.leoko.advancedban.manager.DatabaseManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Leo on 07.08.2017.
 */

public class DatabaseTest {
    @BeforeAll
    public static void setupUniversal(){
        Universal.get().setup(new TestMethods());
    }

    @Test
    public void shouldAutomaticallyDetectDatabaseType(){
        assertFalse("By default no connection with MySQL should be established as it's disabled", DatabaseManager.get().isUseMySQL() );
        assertFalse("MySQL should not be failed as it should not even try establishing any connection", DatabaseManager.get().isFailedMySQL());
        assertTrue("The HSQLDB-Connection should be valid", DatabaseManager.get().isConnectionValid(3));
        DatabaseManager.get().shutdown();
        DatabaseManager.get().setup(true);
        assertFalse("Because of a failed connection MySQL should be disabled", DatabaseManager.get().isUseMySQL() );
        assertTrue("MySQL should be failed as the connection can not succeed", DatabaseManager.get().isFailedMySQL());
    }

    @AfterAll
    public static void shutdownUniversal(){
        Universal.get().shutdown();
    }
}
