package ru.variousvar.moneytransfer.dao;

import ru.variousvar.moneytransfer.model.Transaction;

import java.util.List;

public interface TransactionDao {

    /**
     * Loads one transaction by identifier.
     *
     * @param id transaction identifier
     * @return loaded transaction
     * @throws Exception if account with {@param accountId} doesn't exist or any exception occurred
     */
    Transaction get(Long id) throws Exception;

    /**
     * Loads all registered transactions by all accounts.
     *
     * @return loaded transaction or empty list if none registered
     * @throws Exception if any exception occurred
     */
    List<Transaction> getAll() throws Exception;

    /**
     * Loads all transactions, registered by account with {@param accountId} identifier, either of account being in
     * from or to part of transaction.
     *
     * @param accountId account identifier to select transactions
     * @return loaded transaction registered of specified account
     * @throws Exception if account with {@param accountId} doesn't exist or any exception occurred
     */
    List<Transaction> getAllByAccount(Long accountId) throws Exception;

    /**
     * Runs transaction between accounts.
     *
     * @param transaction transaction to be executed
     * @throws Exception if withdrawn account has not enough money, any of accounts doesn't exist or any exception occurred
     */
    void executeTransaction(Transaction transaction) throws Exception;

}
