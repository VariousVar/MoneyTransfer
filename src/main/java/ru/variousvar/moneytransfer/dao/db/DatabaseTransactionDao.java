package ru.variousvar.moneytransfer.dao.db;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.variousvar.moneytransfer.dao.TransactionDao;
import ru.variousvar.moneytransfer.model.Account;
import ru.variousvar.moneytransfer.model.Transaction;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation, that works with SQL-based database.
 */
public class DatabaseTransactionDao implements TransactionDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseTransactionDao.class);

    private final String selectSingleTransactionWithAccountsQuery = "" +
            "SELECT t.id, t.sender, t.receiver, t.amount, t.created, t.description " +
            "FROM `transaction` t " +
            "LEFT JOIN account aFrom ON aFrom.id = t.sender " +
            "LEFT JOIN account aTo ON aTo.id = t.receiver " +
            "WHERE t.id = ?";

    // fixme left join... may create empty sender, but sender maybe null if we don't have service account to initiate balance
    private final String selectTransactionsWithAccountsQuery = "" +
            "SELECT t.id, t.sender, t.receiver, t.amount, t.created, t.description " +
            "FROM `transaction` t " +
            "LEFT JOIN account aFrom ON aFrom.id = t.sender " +
            "LEFT JOIN account aTo ON aTo.id = t.receiver " +
            "WHERE (t.sender = ? OR t.receiver = ?) " +
            "ORDER BY t.created DESC";

    private final ConnectionFactory connectionFactory;

    public DatabaseTransactionDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Transaction get(Long id) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(selectSingleTransactionWithAccountsQuery);
            statement.setLong(1, id);

            Transaction transaction = null;

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    transaction = new Transaction();

                    transaction.setId(rs.getLong("id"));
                    transaction.setAmount(rs.getLong("amount"));
                    transaction.setDescription(rs.getString("description"));
                    transaction.setCreated(rs.getTimestamp("created"));

                    long senderId = rs.getLong("sender");
                    if (!rs.wasNull()) {
                        transaction.setSender(senderId);
                    }

                    long receiverId = rs.getLong("receiver");
                    if (!rs.wasNull()) {
                        transaction.setReceiver(receiverId);
                    }
                } else {
                    throw new Exception("Transaction with specified id="+ id + " doesn't exist");
                }
            }

            return transaction;
        } catch (SQLException ex) {
            LOGGER.error("Transaction loading failed, exception occurred: {}", ex.getMessage(), ex);
            throw new Exception("Unable to load transaction with id=" + id+ " : " + ex.getMessage(), ex);
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
        }
    }

    @Override
    public List<Transaction> getAllByAccount(Long accountId) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;

        List<Transaction> transactions = new ArrayList<>();

        try {
            connection = getConnection();
            statement = connection.prepareStatement(selectTransactionsWithAccountsQuery);
            statement.setLong(1, accountId);
            statement.setLong(2, accountId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = new Transaction();
                    transaction.setId(rs.getLong("id"));
                    transaction.setAmount(rs.getLong("amount"));
                    transaction.setDescription(rs.getString("description"));
                    transaction.setCreated(rs.getTimestamp("created"));

                    long senderId = rs.getLong("sender");
                    if (!rs.wasNull()) {
                        transaction.setSender(senderId);
                    }

                    long receiverId = rs.getLong("receiver");
                    if (!rs.wasNull()) {
                        transaction.setReceiver(receiverId);
                    }

                    transactions.add(transaction);
                }
            }

        } catch (SQLException ex) {
            LOGGER.error("Account transactions loading failed, exception occurred: {}", ex.getMessage(), ex);
            throw new Exception("Unable to load account transactions: " + ex.getMessage(), ex);
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
        }

        return transactions;
    }

    // todo refactor - too long
    @Override
    public Long executeTransaction(Transaction transaction) throws Exception {

        if (transaction.getSender() == null) {
            throw new Exception("Unable to execute transaction with sender account unspecified.");
        }

        if (transaction.getReceiver() == null) {
            throw new Exception("Unable to execute transaction with receiver account unspecified.");
        }

        Long senderId = transaction.getSender();
        Long receiverId = transaction.getReceiver();

        Connection connection = null;
        PreparedStatement lockAccountsStatement = null;
        PreparedStatement updateAccountsStatement = null;
        PreparedStatement registerTransactionStatement = null;

        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            // lock accounts for update - ensure only this connection can change balance
            lockAccountsStatement = connection.prepareStatement("SELECT id, balance FROM account WHERE id IN (?, ?) FOR UPDATE");
            lockAccountsStatement.setLong(1, receiverId);
            lockAccountsStatement.setLong(2, senderId);

            Long receiverBalance = null, senderBalance = null;
            try (ResultSet rs = lockAccountsStatement.executeQuery()) {
                while (rs.next()) {
                    long accountId = rs.getLong("id");
                    long accountBalance = rs.getLong("balance");

                    if (accountId == receiverId) {
                        receiverBalance = accountBalance;
                    }

                    if (accountId == senderId) {
                        senderBalance = accountBalance;
                    }
                }
            }

            // todo it's better to add info in log about amounts and ids, but I guess it's restricted
            if (receiverBalance == null) {
                throw new Exception("Receiver account doesn't exist");
            }
            if (senderBalance == null) {
                throw new Exception("Sender account doesn't exist");
            }

            if (senderBalance - transaction.getAmount() < 0) {
                throw new Exception("Sender account doesn't have enough money for transfer");
            }

            // update balances
            updateAccountsStatement = connection.prepareStatement("" +
                    "UPDATE account SET balance = ? WHERE id = ?; " +
                    "UPDATE account SET balance = ? WHERE id = ?;");

            updateAccountsStatement.setLong(1, senderBalance - transaction.getAmount());
            updateAccountsStatement.setLong(2, senderId);
            updateAccountsStatement.setLong(3, receiverBalance + transaction.getAmount());
            updateAccountsStatement.setLong(4, receiverId);
            updateAccountsStatement.executeUpdate();

            // register transaction
            registerTransactionStatement = connection.prepareStatement("" +
                    "INSERT INTO transaction (sender, receiver, amount, description, created) " +
                    "VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            registerTransactionStatement.setLong(1, senderId);
            registerTransactionStatement.setLong(2, receiverId);
            registerTransactionStatement.setLong(3, transaction.getAmount());
            registerTransactionStatement.setString(4, transaction.getDescription());
            registerTransactionStatement.setTimestamp(5, Timestamp.from(Instant.now()));

            int affectedRows = registerTransactionStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Transaction creation failed, no rows affected.");
            }

            Long registeredTransactionId = null;
            try (ResultSet generatedKeys = registerTransactionStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    registeredTransactionId = (generatedKeys.getLong(1));
                }
                else {
                    throw new SQLException("Transaction creation failed, no id obtained.");
                }
            }

            connection.commit();

            return registeredTransactionId;
        } catch (SQLException ex) {
            LOGGER.error("Transaction register failed, exception occurred: {}", ex.getMessage(), ex);
            throw new Exception("Unable to ", ex);
        } finally {
            DbUtils.closeQuietly(lockAccountsStatement);
            DbUtils.closeQuietly(updateAccountsStatement);
            DbUtils.closeQuietly(registerTransactionStatement);
            DbUtils.closeQuietly(connection);
        }
    }

    private Connection getConnection() throws SQLException {
        return connectionFactory.getConnection();
    }
}
