package ru.variousvar.moneytransfer.dao.db;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class H2DbDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(H2DbDao.class);

    // todo insert from config
    private static final String DB_URL = "jdbc:h2:mem:moneytransfer;DB_CLOSE_DELAY=-1";
    private static final String TEST_DB_URL = "jdbc:h2:mem:moneytransfertest;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "sa";

    // todo don't understand how 'SET EXCLUSIVE' works - it should break multiple connections but they live and execute
    // todo and tests work. Without 'SET EXCLUSIVE' H2 rejects to create more than one connection.
    private static final String createAccountsTableQuery = "SET EXCLUSIVE 2;" +
            "DROP TABLE IF EXISTS account; " +
            "CREATE TABLE account " +
            "(id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
            "name VARCHAR(100), " +
            "balance BIGINT NOT NULL);";
    private static final String createTransactionsTableQuery = "DROP TABLE IF EXISTS transaction; " +
            "CREATE TABLE transaction " +
            "(id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
            "fromAccount BIGINT, " +
            "toAccount BIGINT NOT NULL, " +
            "amount BIGINT NOT NULL, " +
            "description VARCHAR(100), " +
            "created TIMESTAMP  NOT NULL)";


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static Connection getConnectionForTest() throws SQLException {
        return DriverManager.getConnection(TEST_DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void initialiseDatabase() throws Exception {
        initialiseDatabase(DB_URL);
    }

    public static void initialiseTestDatabase() throws Exception {
        initialiseDatabase(TEST_DB_URL);
    }

    private static void initialiseDatabase(String dbUrl) throws Exception {
        Connection connection = null;
        PreparedStatement createTablesStatement = null;

        try {
            connection = DriverManager.getConnection(dbUrl, DB_USER, DB_PASSWORD);
            connection.setAutoCommit(false);
            createTablesStatement = connection.prepareStatement(createAccountsTableQuery + createTransactionsTableQuery);
            createTablesStatement.execute();

            // todo should I do setAutoCommit(true) ? maybe in pool
            connection.commit();

            LOGGER.info("Database on {} was successfully initialised", dbUrl);

        } catch (SQLException ex) {
            LOGGER.error("Database initialising failed due to error: {}", ex.getMessage(), ex);
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException inner) {
                LOGGER.error("SQL Exception occurred while rolling back database init transaction: {}", inner.getMessage(), inner);
            }

            throw new Exception("Database initialising on " + dbUrl + " failed", ex);
        } finally {
            DbUtils.closeQuietly(createTablesStatement);
            DbUtils.closeQuietly(connection);
        }
    }

}
