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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class TransactionDaoTest {

    private AccountDao accountDao;
    private TransactionDao transactionDao;
    private final Account first = new Account();
    private final Account second = new Account();

    @BeforeEach
    public void beforeTest() throws Exception {
        H2DbDao.initialiseTestDatabase();

        accountDao = new DatabaseAccountDao(H2DbDao::getConnectionForTest);
        transactionDao = new DatabaseTransactionDao(H2DbDao::getConnectionForTest);

        // initial account arrange
        first.setName("First");
        first.setBalance(100);

        second.setName("Second");
        second.setBalance(200);

        Long firstAccountId = accountDao.create(first);
        first.setId(firstAccountId);
        Long secondAccountId = accountDao.create(second);
        second.setId(secondAccountId);
    }

    @Test
    public void execute_executedSuccessfully_accountBalanceChanged() throws Exception {
        // arrange
        Transaction transaction = new Transaction();
        transaction.setFromAccount(first);
        transaction.setToAccount(second);
        long transferAmount = first.getBalance() / 2;
        transaction.setAmount(transferAmount);
        transaction.setDescription("Transfer");

        // act
        Long transactionId = transactionDao.executeTransaction(transaction);
        transaction.setId(transactionId);

        // assert
        Account dbFirstAccount = accountDao.get(first.getId());
        Account dbSecondAccount = accountDao.get(second.getId());
        Transaction dbTransaction = transactionDao.get(transactionId);

        assertThat(dbFirstAccount.getBalance(), equalTo(first.getBalance() - transferAmount));
        assertThat(dbSecondAccount.getBalance(), equalTo(second.getBalance() + transferAmount));

        assertThat(dbTransaction.getAmount(), equalTo(transferAmount));
        assertThat(dbTransaction.getFromAccount().getId(), equalTo(first.getId()));
        assertThat(dbTransaction.getToAccount().getId(), equalTo(second.getId()));

        assertThat(dbTransaction.getDescription(), equalTo(transaction.getDescription()));

    }

    @Test
    public void execute_notEnoughMoney_failToExecuteAndBalanceStaySame() throws Exception {
        // arrange
        Transaction transaction = new Transaction();
        transaction.setFromAccount(first);
        transaction.setToAccount(second);
        long transferAmount = first.getBalance() * 10;
        transaction.setAmount(transferAmount);
        transaction.setDescription("Transfer");


        // act & assert I
        Assertions.assertThrows(Exception.class, () -> transactionDao.executeTransaction(transaction));

        // assert II
        Account dbFirstAccount = accountDao.get(first.getId());
        Account dbSecondAccount = accountDao.get(second.getId());

        assertThat(dbFirstAccount.getBalance(), equalTo(first.getBalance()));
        assertThat(dbSecondAccount.getBalance(), equalTo(second.getBalance()));
    }

    @Test
    public void execute_transferToOrFromNotExistAccount_failToExecuteAndBalanceStaySame() throws Exception {
        // arrange
        Account notExist = new Account();
        notExist.setId(-1L);
        notExist.setName("Fake");
        notExist.setBalance(100_000);

        Transaction transaction = new Transaction();
        transaction.setFromAccount(notExist);
        transaction.setToAccount(second);
        long transferAmount = 50;
        transaction.setAmount(transferAmount);
        transaction.setDescription("Transfer");


        // act & assert I
        Assertions.assertThrows(Exception.class, () -> transactionDao.executeTransaction(transaction));

        // assert II
        Account dbFirstAccount = accountDao.get(first.getId());
        Account dbSecondAccount = accountDao.get(second.getId());

        assertThat(dbFirstAccount.getBalance(), equalTo(first.getBalance()));
        assertThat(dbSecondAccount.getBalance(), equalTo(second.getBalance()));
    }

    @Test
    public void get_executedSuccessfully_exist() throws Exception {
        // arrange
        Transaction transaction = new Transaction();
        transaction.setFromAccount(first);
        transaction.setToAccount(second);
        long transferAmount = 50;
        transaction.setAmount(transferAmount);
        transaction.setDescription("Transfer");

        // act
        Long transactionId = transactionDao.executeTransaction(transaction);
        transaction.setId(transactionId);

        // assert
        Transaction dbTransaction = transactionDao.get(transactionId);

        assertThat(dbTransaction.getAmount(), equalTo(transferAmount));
        assertThat(dbTransaction.getFromAccount().getId(), equalTo(first.getId()));
        assertThat(dbTransaction.getToAccount().getId(), equalTo(second.getId()));
    }

    @Test
    public void getByAccount_multipleTransactionRegistered_onlyAccountTransactions() throws Exception {
        // todo need impl
    }
}
