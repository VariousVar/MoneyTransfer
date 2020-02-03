package ru.variousvar.moneytransfer.dao.db;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.variousvar.moneytransfer.dao.AccountDao;
import ru.variousvar.moneytransfer.model.Account;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation, that works with SQL-based database.
 */
public class DatabaseAccountDao implements AccountDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseAccountDao.class);

    private final String getAccountQuery = "SELECT * FROM account WHERE id = ?";
    private final String getAllAccountsQuery = "SELECT * FROM account";

    private final String lockAccountForUpdateQuery = getAccountQuery + " FOR UPDATE";

    private final String createAccountQuery = "INSERT INTO account (name, balance) VALUES (?, ?)";
    private final String initialBalanceTransactionQuery =
            "INSERT INTO transaction " +
            "(fromAccount, toAccount, amount, description, created) " +
            "VALUES (?, ?, ?, ?, ?)";
    private final String updateAccountInformation = "UPDATE account SET name = ? WHERE id = ?";
    private final String deleteAccountQuery = "DELETE FROM account WHERE id = ?";

    @Override
    public Account get(Long id) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(getAllAccountsQuery);
            Account account = null;

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    account = new Account();
                    account.setId(resultSet.getLong("id"));
                    account.setName(resultSet.getString("name"));
                    account.setBalance(resultSet.getLong("balance"));
                } else {
                    throw new Exception("Account with specified id="+ id + " doesn't exist");
                }
            }

            return account;
        } catch (SQLException ex) {
            LOGGER.error("Accounts loading failed, exception occurred: {}", ex.getMessage(), ex);
            throw new Exception("Unable to load all accounts: " + ex.getMessage(), ex);
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
        }
    }

    @Override
    public Collection<Account> getAll() throws Exception {

        Connection connection = null;
        PreparedStatement statement = null;

        List<Account> accounts = new ArrayList<>();

        try {
            connection = getConnection();
            statement = connection.prepareStatement(getAllAccountsQuery);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Account account = new Account();
                    account.setId(resultSet.getLong("id"));
                    account.setName(resultSet.getString("name"));
                    account.setBalance(resultSet.getLong("balance"));

                    accounts.add(account);
                }
            }

        } catch (SQLException ex) {
            LOGGER.error("Accounts loading failed, exception occurred: {}", ex.getMessage(), ex);
            throw new Exception("Unable to load all accounts: " + ex.getMessage(), ex);
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
        }

        return accounts;
    }

    @Override
    public Long create(Account newAccount) throws Exception {
        Long newAccountId = null;

        Connection connection = null;
        PreparedStatement accountCreationStatement = null;
        PreparedStatement initialTransactionStatement = null;

        try {
            connection = getConnection();

            // create account and receive it's id
            accountCreationStatement = connection.prepareStatement(createAccountQuery, Statement.RETURN_GENERATED_KEYS);

            accountCreationStatement.setString(1, newAccount.getName());
            accountCreationStatement.setLong(2, newAccount.getBalance());

            int affectedRows = accountCreationStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Account creation failed, no rows affected.");
            }

            try (ResultSet generatedKeys = accountCreationStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    newAccountId = (generatedKeys.getLong(1));
                }
                else {
                    throw new SQLException("Account creation failed, no id obtained.");
                }
            }

            // create initial transaction for account balance setup
            initialTransactionStatement = connection.prepareStatement(initialBalanceTransactionQuery);
            initialTransactionStatement.setObject(1, null);
            initialTransactionStatement.setLong(2, newAccountId);
            initialTransactionStatement.setLong(3, newAccount.getBalance());
            initialTransactionStatement.setString(4, "Initial");
            initialTransactionStatement.setTimestamp(5, Timestamp.from(Instant.now())); // todo [IMPORTANT] possible timezone mess

            int transactionsCreated = initialTransactionStatement.executeUpdate();

            if (transactionsCreated == 0) {
                throw new SQLException("Initial transaction creation failed, no rows affected.");
            }

            connection.commit();

            return newAccountId;

        } catch (SQLException ex) {
            LOGGER.error("Account creation failed, exception occurred: {}", ex.getMessage(), ex);
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException rollingBackEx) {
                LOGGER.error("SQL Exception occurred while rolling back account creation: {}",
                        rollingBackEx.getMessage(), rollingBackEx);
            }

            throw new Exception(
                    String.format("Unable to create user account with name={}, and balance={}, sql exception occurred: {}",
                    newAccount.getName(), newAccount.getBalance(), ex.getMessage()), ex);
        } finally {
            DbUtils.closeQuietly(accountCreationStatement);
            DbUtils.closeQuietly(initialTransactionStatement);
            DbUtils.closeQuietly(connection);
        }

    }

    @Override
    public void update(Account account) throws Exception {

    }

    @Override
    public void delete(Long id) throws Exception {

    }

    private Connection getConnection() throws SQLException {
        return H2DbDao.getConnection();
    }
}
