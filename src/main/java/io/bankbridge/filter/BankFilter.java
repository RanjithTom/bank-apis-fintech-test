package io.bankbridge.filter;

import io.bankbridge.errorhandler.BanksCustomException;
import org.eclipse.jetty.http.HttpStatus;
import spark.QueryParamsMap;
import spark.Request;
import spark.utils.StringUtils;

public class BankFilter {

  /**
   * Filter for validating the request values.
   * Filter will do validations for pageNo and size
   * @param request
   */
  public static void validateInput(Request request) {
    QueryParamsMap map = request.queryMap();
    try {
      if (StringUtils.isNotEmpty(map.value("pageNo"))) {
        if (Integer.parseInt(map.value("pageNo")) < 0) {
          throw new BanksCustomException(HttpStatus.BAD_REQUEST_400,
              "Page number is not valid!");
        }
      }
      if (StringUtils.isNotEmpty(map.value("size"))) {
        if (Integer.parseInt(map.value("size")) < 0) {
          throw new BanksCustomException(HttpStatus.BAD_REQUEST_400, "Page size is not valid!");
        }
      }
    } catch (NumberFormatException ne) {
      throw new BanksCustomException(HttpStatus.BAD_REQUEST_400, "pageNo and size should be integer values!");
    }
  }
}
