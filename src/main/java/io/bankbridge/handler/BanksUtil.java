package io.bankbridge.handler;

import io.bankbridge.response.BankDetails;
import spark.QueryParamsMap;
import spark.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BanksUtil {

  private static final int PAGE_SIZE = 5;

  /**
   * Method to create common predicates for countryCode, id, and name.
   * This can be used commonly in both static and remote banks.
   * @param queryParamMap
   * @return
   */
  public static List<Predicate<BankDetails>> getCommonPredicates(Map<String, String> queryParamMap) {
    List<Predicate<BankDetails>> allPredicates = new ArrayList<>();
    if (queryParamMap.containsKey("countrycode")) {
      allPredicates.add(bank -> queryParamMap.get("countrycode").equals(bank.getCountryCode()));
    }
    if (queryParamMap.containsKey("id")) {
      allPredicates.add(bank -> queryParamMap.get("id").equals(bank.getId()));
    }
    if (queryParamMap.containsKey("name")) {
      allPredicates.add(bank -> queryParamMap.get("name").equals(bank.getName()));
    }
    return allPredicates;
  }

  /**
   * Converting the Map<String, String[] -> Map<String, String>
   * if the values in an array then in all filter mechanism there need to stream the array.
   * To avoid this
   * @param queryMap
   * @return
   */
  public static Map<String, String> getParamsMap(Map<String, String[]> queryMap) {
    Map<String, String> paramsMap = new HashMap<>();
    queryMap.entrySet().stream().forEach(entry -> {
      paramsMap.put(entry.getKey(), Arrays.stream(entry.getValue()).findFirst().get());});
    return paramsMap;
  }

  /**
   * Common method used for paginating the response based on size and pagenumber
   * if size is given it will return the list with defined size same for pageNo also.
   * By using stream skip and limit features.
   * @param bankDetails
   * @param queryParamMap
   * @return
   */
  public static List<BankDetails> paginatedResponse(List<BankDetails> bankDetails,
      Map<String, String> queryParamMap) {
    int pageSize =
        queryParamMap.containsKey("size") ? Integer.valueOf(queryParamMap.get("size")) : PAGE_SIZE;
    int pageNo =
        queryParamMap.containsKey("pageNo") ? Integer.valueOf(queryParamMap.get("pageNo")) : 0;
    return bankDetails.stream()
        .skip(pageNo * pageSize)
        .limit(pageSize)
        .collect(Collectors.toList());
  }

}
