package ru.variousvar.moneytransfer.web;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import ru.variousvar.moneytransfer.dao.TransactionDao;
import ru.variousvar.moneytransfer.model.Account;
import ru.variousvar.moneytransfer.model.Transaction;
import ru.variousvar.moneytransfer.web.dto.ExecuteTransactionDto;

/**
 * Implements REST API for Transaction model.
 */
public class TransactionController {

    private final Router router;
    private final TransactionDao transactionDao;

    public TransactionController(Router router, TransactionDao transactionDao) {
        this.router = router;
        this.transactionDao = transactionDao;
    }

    public void init() {
        router.route("/transaction/*").handler(BodyHandler.create()).handler(ResponseContentTypeHandler.create());
        router.route(HttpMethod.POST, "/transaction/").handler(this::executeTransaction).produces("application/json");
    }

    private void executeTransaction(RoutingContext rc) {
        try {
            ExecuteTransactionDto transactionDto = rc.getBodyAsJson().mapTo(ExecuteTransactionDto.class);

            Transaction transaction = new Transaction();
            transaction.setSender(transactionDto.getSender());
            transaction.setReceiver(transactionDto.getReceiver());
            transaction.setAmount(transactionDto.getAmount());
            transaction.setDescription(transactionDto.getDescription());

            Long transactionId = transactionDao.executeTransaction(transaction);
            transaction.setId(transactionId);

            // todo bad situation - transaction created, but in case of exception here user will think it wasn't
            rc.response().end(Json.encodePrettily(transaction));
        } catch (Exception e) {
            rc.response().setStatusCode(400);
            ErrorResponse error = new ErrorResponse();
            error.setMessage("Unable to execute transaction");
            rc.response().end(Json.encodePrettily(error));
        }
    }
}
