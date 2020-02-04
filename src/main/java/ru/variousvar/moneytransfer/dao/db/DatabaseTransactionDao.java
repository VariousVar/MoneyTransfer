package ru.variousvar.moneytransfer.dao.db;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.variousvar.moneytransfer.dao.TransactionDao;
import ru.variousvar.moneytransfer.model.Account;
import ru.variousvar.moneytransfer.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation, that works with SQL-based database.
 */
public class DatabaseTransactionDao implements TransactionDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseTransactionDao.class);

    // todo probably it's unnecessary to read account, because user already selected it
    // fixme left join... may create empty fromAccount, but fromAccount maybe null if we don't have service account to initiate balance
    private final String selectTransactionsWithAccountsQuery = "" +
            "SELECT t.id, t.fromAccount, t.toAccount, t.amount, t.created, t.description " +
            "FROM `transaction` t " +
            "LEFT JOIN account aFrom ON aFrom.id = t.fromAccount " +
            "INNER JOIN account aTo ON aTo.id = t.toAccount " +
            "WHERE (t.fromAccount = ? OR t.toAccount = ?) " +
            "ORDER BY t.created DESC";

    @Override
    public Transaction get(Long id) throws Exception {
        return null;
    }

    @Override
    public List<Transaction> getAll() throws Exception {
        return Collections.emptyList();
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
                    Account fromAccount = new Account();
                    Account toAccount = new Account();

                    transaction.setId(rs.getLong("id"));
                    transaction.setAmount(rs.getLong("amount"));
                    transaction.setDescription(rs.getString("description"));

                    long fromAccountId = rs.getLong("fromAccount");
                    if (!rs.wasNull()) {
                        fromAccount.setId(fromAccountId);
                        transaction.setFromAccount(fromAccount);
                    }

                    toAccount.setId(rs.getLong("toAccount"));
                    transaction.setToAccount(toAccount);

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

    @Override
    public void executeTransaction(Transaction transaction) throws Exception {

    }

    private Connection getConnection() throws SQLException {
        return H2DbDao.getConnection();
    }
}
