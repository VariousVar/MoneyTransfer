package ru.variousvar.moneytransfer.web;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import ru.variousvar.moneytransfer.dao.AccountDao;
import ru.variousvar.moneytransfer.dao.TransactionDao;
import ru.variousvar.moneytransfer.model.Account;
import ru.variousvar.moneytransfer.model.Transaction;
import ru.variousvar.moneytransfer.web.dto.CreateAccountDto;

import java.util.Collection;
import java.util.List;

/**
 * Implements REST API for Account model.
 */
public class AccountController {

    private final Router router;
    private final AccountDao accountDao;
    private final TransactionDao transactionDao;

    public AccountController(Router router, AccountDao accountDao, TransactionDao transactionDao) {
        this.router = router;
        this.accountDao = accountDao;
        this.transactionDao = transactionDao;
    }

    public void init() {
        router.route("/account/*").handler(BodyHandler.create()).handler(ResponseContentTypeHandler.create());

        router.route(HttpMethod.GET, "/account/").handler(this::allAccounts).produces("application/json");
        router.route(HttpMethod.GET, "/account/:id").handler(this::singleAccount).produces("application/json");
        router.route(HttpMethod.POST, "/account/").handler(this::createAccount).produces("application/json");
        router.route(HttpMethod.DELETE, "/account/:id").handler(this::deleteAccount).produces("application/json");
        router.route(HttpMethod.PATCH, "/account/:id").handler(this::changeAccount).produces("application/json");
        router.route(HttpMethod.GET, "/account/:id/transaction/").handler(this::allAccountTransactions).produces("application/json");

    }

    private void createAccount(RoutingContext rc) {

        CreateAccountDto createAccountDto = rc.getBodyAsJson().mapTo(CreateAccountDto.class);

        Account account = new Account();
        account.setBalance(createAccountDto.getBalance());
        account.setName(createAccountDto.getName());

        try {
            Long accountId = accountDao.create(account);
            account.setId(accountId);

            rc.response().end(Json.encodePrettily(account));
        } catch (Exception e) {
            rc.response().setStatusCode(400);
            ErrorResponse error = new ErrorResponse();
            error.setMessage("Unable to create account");
            rc.response().end(Json.encodePrettily(error));
        }

    }

    private void allAccounts(RoutingContext rc) {
        try {
            Collection<Account> accounts = accountDao.getAll();
            rc.response().end(Json.encodePrettily(accounts));
        } catch (Exception e) {
            rc.response().setStatusCode(400);
            ErrorResponse error = new ErrorResponse();
            error.setMessage("Unable to obtain all accounts");
            rc.response().end(Json.encodePrettily(error));
        }
    }

    private void singleAccount(RoutingContext rc) {

        try {
            Long id = Long.valueOf(rc.request().getParam("id"));
            Account account = accountDao.get(id);
            rc.response().end(Json.encodePrettily(account));
        } catch (Exception e) {
            rc.response().setStatusCode(400);
            ErrorResponse error = new ErrorResponse();
            error.setMessage("Unable to obtain account");
            rc.response().end(Json.encodePrettily(error));
        }
    }

    private void deleteAccount(RoutingContext rc) {
        try {
            Long id = Long.valueOf(rc.request().getParam("id"));
            accountDao.delete(id);
        } catch (Exception e) {
            rc.response().setStatusCode(400);
            ErrorResponse error = new ErrorResponse();
            error.setMessage("Unable to delete account");
            rc.response().end(Json.encodePrettily(error));
        }
    }

    private void changeAccount(RoutingContext rc) {
        try {
            Long id = Long.valueOf(rc.request().getParam("id"));
            Account account = accountDao.get(id);
            CreateAccountDto changeAccountDto = rc.getBodyAsJson().mapTo(CreateAccountDto.class);

            account.setName(changeAccountDto.getName());
            accountDao.update(account);

            rc.response().end(Json.encodePrettily(account));
        } catch (Exception e) {
            rc.response().setStatusCode(400);
            ErrorResponse error = new ErrorResponse();
            error.setMessage("Unable to change account");
            rc.response().end(Json.encodePrettily(error));
        }
    }

    private void allAccountTransactions(RoutingContext rc) {
        Long id = Long.valueOf(rc.request().getParam("id"));
        try {
            List<Transaction> allByAccount = transactionDao.getAllByAccount(id);
            rc.response().end(Json.encodePrettily(allByAccount));
        } catch (Exception e) {
            rc.response().setStatusCode(400);
            ErrorResponse error = new ErrorResponse();
            error.setMessage("Unable to obtain all transaction by account");
            rc.response().end(Json.encodePrettily(error));
        }
    }
}
