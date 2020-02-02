package ru.variousvar.moneytransfer.dao.db;

import ru.variousvar.moneytransfer.dao.TransactionDao;
import ru.variousvar.moneytransfer.model.Transaction;

import java.util.Collections;
import java.util.List;

/**
 * Implementation, that works with SQL-based database.
 */
public class DatabaseTransactionDao implements TransactionDao {

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
        return null;
    }

    @Override
    public void executeTransaction(Transaction transaction) throws Exception {

    }
}
