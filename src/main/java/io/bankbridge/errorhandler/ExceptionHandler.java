package io.bankbridge.errorhandler;

import static spark.Spark.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExceptionHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String CONTENT_TYPE = "application/json";
    private static final String CUSTOM_ERROR_MSG = "{\"message\":\"%s\"}";

    public static void errorHandler() {

      exception(BanksCustomException.class, (exception, req, res) -> {
        res.status(exception.getStatus());
        res.type(CONTENT_TYPE);
        ErrorResponse errorResponse = new ErrorResponse(exception.getMessage());
        try {
          res.body(objectMapper.writeValueAsString(errorResponse));
        } catch (JsonProcessingException jsonProcessingException) {
          res.body(String.format(CUSTOM_ERROR_MSG, "Not able to process the request"));
        }
      });

      notFound((req, res) -> {
        res.type(CONTENT_TYPE);
        return String.format(CUSTOM_ERROR_MSG, "Requested resource not found in the system");
      });

      internalServerError((req, res) -> {
        res.type(CONTENT_TYPE);
        return String.format(CUSTOM_ERROR_MSG, "Internal Error from the system");
      });
    }


}
