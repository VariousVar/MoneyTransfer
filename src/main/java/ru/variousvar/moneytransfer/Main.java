package ru.variousvar.moneytransfer;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import ru.variousvar.moneytransfer.dao.AccountDao;
import ru.variousvar.moneytransfer.dao.TransactionDao;
import ru.variousvar.moneytransfer.dao.db.DatabaseAccountDao;
import ru.variousvar.moneytransfer.dao.db.DatabaseTransactionDao;
import ru.variousvar.moneytransfer.dao.db.H2DbDao;
import ru.variousvar.moneytransfer.web.AccountController;
import ru.variousvar.moneytransfer.web.TransactionController;

public class Main {
    public static void main(String[] args) throws Exception {
        H2DbDao.initialiseDatabase();

        AccountDao accountDao = new DatabaseAccountDao(H2DbDao::getConnection);
        TransactionDao transactionDao = new DatabaseTransactionDao(H2DbDao::getConnection);

        Vertx vertx = Vertx.vertx();
        Router router = Router.router(vertx);
        new AccountController(router, accountDao, transactionDao).init();
        new TransactionController(router, transactionDao).init();

        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(router).listen(8080);
    }
}
