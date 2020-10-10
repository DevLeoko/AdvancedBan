package me.leoko.advancedban.utils.abstraction;

import me.leoko.advancedban.manager.LogManager;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.function.Consumer;

public class Logger {
    private final Consumer<String> consoleLogger;
    private final File debugFile;
    private boolean debugMode = false; //TODO

    public Logger(Consumer<String> consoleLogger, File rootFolder) {
        this.consoleLogger = consoleLogger;
        this.debugFile = new File(rootFolder, "logs/latest.log");
    }

    public void debug(boolean enable){
        debugMode = enable;
    }

    /**
     * Log without AB prefix.
     *
     * @param msg the msg
     */
    public void directLog(String msg) {
        consoleLogger.accept(msg);
        debugToFile(msg);
    }

    /**
     * Log.
     *
     * @param msg the msg
     */
    public void log(String msg) {
        consoleLogger.accept("§8[§cAdvancedBan§8] §7" + msg);
        debugToFile(msg);
    }

    /**
     * Debug.
     *
     * @param msg the msg
     */
    public void debug(String msg) {
        if (debugMode)
            consoleLogger.accept("§8[§cAdvancedBan§8] §cDebug: §7" + msg.toString());

        debugToFile(msg);
    }

    public void debugException(Exception exc) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exc.printStackTrace(pw);
        debug(sw.toString());
    }

    /**
     * Debug.
     *
     * @param ex the ex
     */
    public void debugSqlException(SQLException ex) {
        log("§7An error has occurred with the database, the error code is: '" + ex.getErrorCode() + "'");
        log("§7The state of the sql is: " + ex.getSQLState());
        log("§7Error message: " + ex.getMessage());

        debugException(ex);
    }

    private void debugToFile(String msg) {
        if (!debugFile.exists()) {
            try {
                debugFile.createNewFile();
            } catch (IOException ex) {
                System.out.print("An error has occurred creating the 'latest.log' file again, check your server.");
                System.out.print("Error message" + ex.getMessage());
            }
        } else {
            LogManager.getInstance().checkLastLog(false);
        }
        try {
            FileUtils.writeStringToFile(debugFile, "[" + new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()) + "] " + msg.replace("§", "") + "\n", "UTF8", true);
        } catch (IOException ex) {
            System.out.print("An error has occurred writing to 'latest.log' file.");
            System.out.print(ex.getMessage());
        }
    }

    public File getDebugFile() {
        return debugFile;
    }
}
