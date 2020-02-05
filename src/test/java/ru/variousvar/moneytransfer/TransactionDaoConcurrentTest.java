package ru.variousvar.moneytransfer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.variousvar.moneytransfer.dao.AccountDao;
import ru.variousvar.moneytransfer.dao.TransactionDao;
import ru.variousvar.moneytransfer.dao.db.DatabaseAccountDao;
import ru.variousvar.moneytransfer.dao.db.DatabaseTransactionDao;
import ru.variousvar.moneytransfer.dao.db.H2DbDao;
import ru.variousvar.moneytransfer.model.Account;
import ru.variousvar.moneytransfer.model.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class TransactionDaoConcurrentTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionDaoConcurrentTest.class);
    private AccountDao accountDao;
    private TransactionDao transactionDao;

    @BeforeEach
    public void beforeTest() throws Exception {
        H2DbDao.initialiseTestDatabase();

        accountDao = new DatabaseAccountDao(H2DbDao::getConnectionForTest);
        transactionDao = new DatabaseTransactionDao(H2DbDao::getConnectionForTest);
    }

    @Test
    public void executeTransaction_multipleTransactionsOnSameAccount_balanceStaysConsistent() throws Exception {
        int concurrentTransactions = 20;

        // arrange
        Account sender = new Account();
        sender.setName("First");
        sender.setBalance(100);

        Long firstAccountId = accountDao.create(sender);
        sender.setId(firstAccountId);

        List<Account> accountsToTransfer = new ArrayList<>();
        for (int i = 0; i < concurrentTransactions; i++) {
            Account account = new Account();
            account.setName("Account-"+i);
            account.setBalance(500);

            Long accountId = accountDao.create(account);
            account.setId(accountId);

            accountsToTransfer.add(account);
        }

        ExecutorService executor = Executors.newFixedThreadPool(concurrentTransactions);

        AtomicInteger rejectedTransactions = new AtomicInteger();
        AtomicReference<Long> successfulTransaction = new AtomicReference<>();

        // act
        accountsToTransfer.forEach(a -> executor.submit(() -> {
            Transaction transaction = new Transaction();
            transaction.setFromAccount(sender);
            transaction.setToAccount(a);
            transaction.setAmount((long) (sender.getBalance() * 0.9));
            transaction.setDescription("TransferTo-"+a.getName());

            try {
                Long successfulTransactionId = transactionDao.executeTransaction(transaction);
                successfulTransaction.set(successfulTransactionId);
                LOGGER.info("Transaction '{}' was successfully executed.", transaction.getDescription());
            } catch (Exception e) {
                LOGGER.error("Failed to execute transaction '{}', error = {}", transaction.getDescription(), e.getMessage());
                rejectedTransactions.incrementAndGet();
            }
        }));

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        // assert
        assertThat(rejectedTransactions.get(), equalTo(concurrentTransactions - 1));
        assertThat(successfulTransaction.get(), notNullValue());

    }
}
