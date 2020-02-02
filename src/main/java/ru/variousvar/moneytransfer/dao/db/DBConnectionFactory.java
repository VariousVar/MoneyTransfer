package ru.variousvar.moneytransfer.dao.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionFactory {
    // todo insert from config
    private static final String DB_URL = "";
    private static final int DB_PORT = 9000;
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }


}
