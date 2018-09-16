import me.leoko.advancedban.AdvancedBan;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Leo on 07.08.2017.
 */

public class DatabaseTest {
    private AdvancedBan advancedBan = new TestAdvancedBan();

    @Before
    public void onEnable() {
        advancedBan.onEnable();
    }

    @Test
    public void shouldAutomaticallyDetectDatabaseType(){
        assertFalse("By default no connection with MySQL should be established as it's disabled", advancedBan.getDatabaseManager().isUseMySQL());
        assertFalse("MySQL should not be failed as it should not even try establishing any connection", advancedBan.getDatabaseManager().isFailedMySQL());
        assertTrue("The HSQLDB-Connection should be valid", advancedBan.getDatabaseManager().isConnectionValid(3));
        advancedBan.getDatabaseManager().onDisable();
        advancedBan.getDatabaseManager().onEnable();
        assertFalse("Because of a failed connection MySQL should be disabled", advancedBan.getDatabaseManager().isUseMySQL());
        assertTrue("MySQL should be failed as the connection can not succeed", advancedBan.getDatabaseManager().isFailedMySQL());
    }

    @After
    public void onDisable() {
        advancedBan.onDisable();
    }
}