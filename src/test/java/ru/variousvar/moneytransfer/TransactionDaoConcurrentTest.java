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
import java.util.stream.Collectors;

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

        Long senderAccountId = accountDao.create(sender);
        sender.setId(senderAccountId);

        long transferAmount = (long) (sender.getBalance() * 0.9);
        long receiversAccountsBalance = 500;

        List<Account> accountsToTransfer = new ArrayList<>();
        for (int i = 0; i < concurrentTransactions; i++) {
            Account account = new Account();
            account.setName("Account-"+i);
            account.setBalance(receiversAccountsBalance);

            Long accountId = accountDao.create(account);
            account.setId(accountId);

            accountsToTransfer.add(account);
        }

        ExecutorService executor = Executors.newFixedThreadPool(concurrentTransactions);

        AtomicInteger rejectedTransactionsCounter = new AtomicInteger();
        AtomicReference<Long> successfulTransactionId = new AtomicReference<>();

        // act
        accountsToTransfer.forEach(a -> executor.submit(() -> {
            Transaction transaction = new Transaction();
            transaction.setSender(sender.getId());
            transaction.setReceiver(a.getId());
            transaction.setAmount(transferAmount);
            transaction.setDescription("TransferTo-"+a.getName());

            try {
                Long transactionId = transactionDao.executeTransaction(transaction);
                successfulTransactionId.set(transactionId);
            } catch (Exception e) {
                rejectedTransactionsCounter.incrementAndGet();
            }
        }));

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        // load all accounts to get actual balances
        Account dbSenderAccount = accountDao.get(senderAccountId);
        List<Account> dbAccounts = accountsToTransfer.stream().map(a -> {
            try {
                return accountDao.get(a.getId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        // assert
        assertThat(rejectedTransactionsCounter.get(), equalTo(concurrentTransactions - 1));
        assertThat(successfulTransactionId.get(), notNullValue());
        assertThat(dbSenderAccount.getBalance(), equalTo(sender.getBalance() - transferAmount));

        // ensure only one account received money, others stayed unchanged
        int accountBalanceIncreasedCount = 0, accountBalanceStaysSameCount = 0;
        for (Account dbAccount : dbAccounts) {
            if (dbAccount.getBalance() == receiversAccountsBalance) {
                accountBalanceStaysSameCount++;
            } else if (dbAccount.getBalance() == receiversAccountsBalance + transferAmount) {
                accountBalanceIncreasedCount++;
            } else {
                throw new Exception("Account balance changed unpredictably. Transfer logic probably really broken.");
            }
        }

        assertThat(accountBalanceIncreasedCount, equalTo(1));
        assertThat(accountBalanceStaysSameCount, equalTo(concurrentTransactions - 1));

    }
}
