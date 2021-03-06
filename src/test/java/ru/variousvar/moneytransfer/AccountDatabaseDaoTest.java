package ru.variousvar.moneytransfer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.variousvar.moneytransfer.dao.AccountDao;
import ru.variousvar.moneytransfer.dao.TransactionDao;
import ru.variousvar.moneytransfer.dao.db.DatabaseAccountDao;
import ru.variousvar.moneytransfer.dao.db.DatabaseTransactionDao;
import ru.variousvar.moneytransfer.dao.db.H2DbDao;
import ru.variousvar.moneytransfer.model.Account;
import ru.variousvar.moneytransfer.model.Transaction;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


public class AccountDatabaseDaoTest {

    private AccountDao accountDao;
    private TransactionDao transactionDao;

    @BeforeEach
    public void beforeTest() throws Exception {
        H2DbDao.initialiseTestDatabase();

        accountDao = new DatabaseAccountDao(H2DbDao::getConnectionForTest);
        transactionDao = new DatabaseTransactionDao(H2DbDao::getConnectionForTest);
    }

    @Test
    public void createAccount_validParameters_created() throws Exception {
        // arrange
        Account account = new Account();
        account.setName("Test1");
        account.setBalance(100);

        // act
        Long accountId = accountDao.create(account);
        Collection<Account> allAccounts = accountDao.getAll();
        Account dbAccount = accountDao.get(accountId);

        // assert
        assertThat(allAccounts, hasSize(1));
        assertThat(dbAccount.getName(), equalTo(account.getName()));
        assertThat(dbAccount.getBalance(), equalTo(account.getBalance()));

    }

    @Test
    public void createAccount_validParameters_shouldExistInitialTransaction() throws Exception {
        // arrange
        Account account = new Account();
        account.setName("Test1");
        account.setBalance(100);

        // act
        Long accountId = accountDao.create(account);
        List<Transaction> accountTransactions = transactionDao.getAllByAccount(accountId);

        // assert
        assertThat(accountTransactions, hasSize(1));
        Transaction accountInitialTransaction = accountTransactions.get(0);
        assertThat(accountInitialTransaction.getAmount(), equalTo(account.getBalance()));
        assertThat(accountInitialTransaction.getReceiver(), equalTo(accountId));
    }

    @Test
    public void createAccount_illegalBalance_rejected() throws Exception {
        // arrange
        Account account = new Account();
        account.setName("Test1");
        account.setBalance(-100);

        // act & assert
        Assertions.assertThrows(Exception.class, () -> accountDao.create(account));
    }

    @Test
    public void getAllAccounts_validCreation_successful() throws Exception {
        // arrange
        Account accountOne = new Account();
        accountOne.setName("Test1");
        accountOne.setBalance(100);

        Account accountTwo = new Account();
        accountTwo.setName("Test2");
        accountTwo.setBalance(200);

        // act
        Long accountOneId = accountDao.create(accountOne);
        Long accountTwoId = accountDao.create(accountTwo);
        Collection<Account> allAccounts = accountDao.getAll();
        Account dbAccountOne = accountDao.get(accountOneId);
        Account dbAccountTwo = accountDao.get(accountTwoId);

        // assert
        assertThat(allAccounts, hasSize(2));
        assertThat(dbAccountOne.getName(), equalTo(accountOne.getName()));
        assertThat(dbAccountOne.getBalance(), equalTo(accountOne.getBalance()));

        assertThat(dbAccountTwo.getName(), equalTo(accountTwo.getName()));
        assertThat(dbAccountTwo.getBalance(), equalTo(accountTwo.getBalance()));
    }

    @Test
    public void updateAccount_onlyName_changed() throws Exception {
        // arrange
        Account account = new Account();
        account.setName("Test1");
        account.setBalance(100);

        // act
        Long accountId = accountDao.create(account);
        account.setId(accountId); // for the test, actual account obviously will have id before update attempt
        account.setName("Test2");
        account.setBalance(200);
        accountDao.update(account);
        Account dbAccount = accountDao.get(accountId);

        // assert
        assertThat(dbAccount.getName(), equalTo(account.getName()));
        assertThat(dbAccount.getBalance(), not(equalTo(account.getBalance())));
    }

    @Test
    public void deleteAccount_existed_successful() throws Exception {
        // arrange
        Account account = new Account();
        account.setName("Test1");
        account.setBalance(100);

        // act
        Long accountId = accountDao.create(account);
        accountDao.delete(accountId);

        // assert
        Assertions.assertThrows(Exception.class, () -> accountDao.get(accountId));
    }

    @Test
    public void deleteAccount_notExisted_unsuccessful() throws Exception {
        // arrange

        // act & assert
        Assertions.assertThrows(Exception.class, () -> accountDao.delete(1L));
    }

    @Test
    public void getByAccount_multipleTransactionRegistered_onlyAccountTransactions() throws Exception {
        // arrange
        Account accountOne = new Account();
        accountOne.setName("Test1");
        accountOne.setBalance(100);

        Account accountTwo = new Account();
        accountTwo.setName("Test2");
        accountTwo.setBalance(200);

        Account accountThree = new Account();
        accountTwo.setName("Test3");
        accountTwo.setBalance(200);

        Long accountOneId = accountDao.create(accountOne);
        Long accountTwoId = accountDao.create(accountTwo);
        Long accountThreeId = accountDao.create(accountThree);

        Transaction transaction = new Transaction();
        transaction.setSender(accountTwoId);
        transaction.setReceiver(accountOneId); // send to account one to ease test check
        transaction.setAmount((long) (accountOne.getBalance() * 0.1));

        transactionDao.executeTransaction(transaction);

        // act
        List<Transaction> allByAccountOne = transactionDao.getAllByAccount(accountOneId);

        // assert
        // check accountThree transactions not present in list, cause no transactions between accountOne and accountThree
        assertThat(allByAccountOne,
                everyItem(allOf(
                        not(hasProperty("sender", is(accountThreeId))),
                        not(hasProperty("receiver", is(accountThreeId))))

                ));
        // check receiver field, cause only initial and one more transactions exist, both have accountOne in receiver
        assertThat(allByAccountOne, everyItem(hasProperty("receiver", is(accountOneId))));

    }

}
