package com.flixr.exceptions;

import java.sql.SQLException;

/**
 * @author Thomas Thompson
 * Used to catch SQLExceptions in DAO and return DAO Exceptions to calling methods
 */
public class DAOException extends SQLException {
    public DAOException(SQLException e) {
        super(e);
    }
    public DAOException(RuntimeException e) {
        super(e);
    }
    public DAOException(NullPointerException e) {
        super(e);
    }
}
