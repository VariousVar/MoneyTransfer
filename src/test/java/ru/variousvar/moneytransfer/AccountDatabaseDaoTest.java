package ru.variousvar.moneytransfer;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.variousvar.moneytransfer.dao.AccountDao;
import ru.variousvar.moneytransfer.dao.TransactionDao;
import ru.variousvar.moneytransfer.dao.db.DatabaseAccountDao;
import ru.variousvar.moneytransfer.dao.db.DatabaseTransactionDao;
import ru.variousvar.moneytransfer.dao.db.H2DbDao;
import ru.variousvar.moneytransfer.model.Account;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


public class AccountDatabaseDaoTest {

    private AccountDao accountDao;
    private TransactionDao transactionDao;

    @BeforeEach
    public void beforeTest() throws Exception {
        H2DbDao.initialiseDatabase(); // todo [IMPORTANT] using real db for fast prototype

        accountDao = new DatabaseAccountDao();
        transactionDao = new DatabaseTransactionDao();
    }

    @Test
    public void createAccount_validParameters_created() throws Exception {
        Account account = new Account();
        account.setName("Test1");
        account.setBalance(100);

        Long accountId = accountDao.create(account);

        Collection<Account> allAccounts = accountDao.getAll();
        Account dbAccount = accountDao.get(accountId);

        assertThat(allAccounts, hasSize(1));
        assertThat(account.getName(), equalTo(dbAccount.getName()));
        assertThat(account.getBalance(), equalTo(dbAccount.getBalance()));

    }

    @Test
    public void createAccount_validParameters_shouldExistInitialTransaction() throws Exception {

    }

    @Test
    public void createAccount_illegalBalance_rejected() throws Exception {

    }

    @Test
    public void getAllAccounts_validCreation_successful() throws Exception {

    }

    @Test
    public void updateAccount_onlyName_changed() throws Exception {

    }

    @Test
    public void deleteAccount_existed_successful() throws Exception {

    }

    @Test
    public void deleteAccount_notExisted_unsuccessful() throws Exception {

    }

}
