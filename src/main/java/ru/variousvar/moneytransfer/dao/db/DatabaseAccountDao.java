package ru.variousvar.moneytransfer.dao.db;

import ru.variousvar.moneytransfer.dao.AccountDao;
import ru.variousvar.moneytransfer.model.Account;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * Implementation, that works with SQL-based database.
 */
public class DatabaseAccountDao implements AccountDao {

    private final String getAccountQuery = "SELECT * FROM account WHERE id = ?";
    private final String getAllAccountsQuery = "SELECT * FROM account";

    private final String lockAccountForUpdateQuery = getAccountQuery + " FOR UPDATE";

    private final String createAccountQuery = "INSERT INTO account (name, amount) VALUES (?, ?)";
    private final String createAccountInitialBalanceTransactionQuery =
            "INSERT INTO transaction " +
            "(fromAccount, toAccount, amount, description, created) " +
            "VALUES (?, ?, ?, ?, ?)";
    private final String updateAccountInformation = "UPDATE account SET name = ? WHERE id = ?";
    private final String deleteAccountQuery = "DELETE FROM account WHERE id = ?";

    @Override
    public Account get(Long id) throws Exception {
        return null;
    }

    @Override
    public Collection<Account> getAll() throws Exception {
        return Collections.emptyList();
    }

    @Override
    public void create(Account newAccount) throws Exception {

    }

    @Override
    public void update(Account account) throws Exception {

    }

    @Override
    public void delete(Long id) throws Exception {

    }

    private Connection getConnection() throws SQLException {
        return DBConnectionFactory.getConnection();
    }
}
