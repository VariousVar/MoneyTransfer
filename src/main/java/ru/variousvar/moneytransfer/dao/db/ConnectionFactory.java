package ru.variousvar.moneytransfer.dao.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Simple interface for connections setup in dao layer. Helps split real and test connections.
 */
@FunctionalInterface
public interface ConnectionFactory {
    Connection getConnection() throws SQLException;
}
