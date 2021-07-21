package io.bankbridge.integration;

import static io.bankbridge.filter.BankFilter.validateInput;
import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;

import io.bankbridge.errorhandler.ExceptionHandler;
import io.bankbridge.handler.BanksCacheBased;
import io.bankbridge.handler.BanksRemoteCalls;
import io.bankbridge.response.BanksResponseTransformer;

public class BanksController {

  public BanksController() throws Exception {

    BanksCacheBased.init();
    //Refactored BanksRemoteCalls little bit with constructor initiation
    BanksRemoteCalls banksRemoteCalls = new BanksRemoteCalls();

    //Filter used for request validations, currently no path pattern is given but we can give pattern matches with required url also
    before("*/banks/*",(request, response) -> validateInput(request));

    /**
     * Also Updated routes with passing queryMap only so from Handler layer it will be reusable in other frameworks too rather than
     * keeping dependency with spark
     */
    get("/v1/banks/all",
        (request, response) -> BanksCacheBased.handle(request.queryMap().toMap()),
        BanksResponseTransformer::render);
    get("/v2/banks/all", (request, response) -> banksRemoteCalls.handle(request.queryMap().toMap()),
        BanksResponseTransformer::render);

    ExceptionHandler.errorHandler();
    after((request, response) -> response.type("application/json"));
  }

}
