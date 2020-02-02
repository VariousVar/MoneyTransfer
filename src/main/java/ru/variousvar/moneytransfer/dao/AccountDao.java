package ru.variousvar.moneytransfer.dao;

import ru.variousvar.moneytransfer.model.Account;

import java.util.Collection;

public interface AccountDao {

    /**
     * Loads one account by identifier.
     *
     * @param id account identifier
     * @return fully loaded account
     * @throws Exception if specified account doesn't exist or any error occurred
     */
    Account get(Long id) throws Exception;

    /**
     * Loads all existed accounts.
     *
     * @return accounts
     * @throws Exception if any error occurred
     */
    Collection<Account> getAll() throws Exception;


    /**
     * Creates account. Automatically registers initial balance transaction.
     *
     * @param newAccount account to be created
     * @throws Exception if account already exist or any error occurred
     */
    void create(Account newAccount) throws Exception;

    /**
     * Updates account properties (except for balance and id).
     *
     * @param account account that contain new values for properties
     * @throws Exception if account doesn't exist or any exception occurred
     */
    void update(Account account) throws Exception;

    /**
     * Deletes account.
     *
     * @param id identifier of account to be deleted
     * @throws Exception if account doesn't exist or any exception occurred
     */
    void delete(Long id) throws Exception;


}
