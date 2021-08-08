package me.leoko.advancedban.utils;

import java.sql.SQLException;

public class UncheckedSQLException extends RuntimeException {

    private static final long serialVersionUID = 8399585059677443159L;

    private static final String REPORT_THIS_ERROR = "An unexpected database-related error has occurred.\n"
            + "If using MySQL, check that your MySQL server is online. Report this "
            + "error in: https://github.com/DevLeoko/AdvancedBan/issues";

    public UncheckedSQLException(SQLException cause) {
        super(REPORT_THIS_ERROR + ". SQLState: " + cause.getSQLState(), cause);
    }

    public UncheckedSQLException(String message, SQLException cause) {
        super(REPORT_THIS_ERROR + ". Message: " + message + ". SQLState: " + cause.getSQLState(), cause);
    }
}
