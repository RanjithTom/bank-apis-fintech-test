package io.bankbridge.handler;

import static io.bankbridge.handler.BanksUtil.getCommonPredicates;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;


import com.fasterxml.jackson.databind.ObjectMapper;

import io.bankbridge.provider.RemoteBanksProvider;
import io.bankbridge.provider.RemoteBanksProviderImpl;
import io.bankbridge.model.BankModel;
import io.bankbridge.response.BankDetails;


public class BanksRemoteCalls {

  private static Map config;
  private RemoteBanksProvider remoteBanksProvider;

  /**
   * Default constructor which initiates BanksRemoteCalls with default RemoteBanksProvider
   * RemoteBanksProvider -> returns the banks details by using parallel call mechanism(RemoteBanksProviderImpl)
   * @throws Exception
   */
  public BanksRemoteCalls() throws Exception {
    this.remoteBanksProvider = new RemoteBanksProviderImpl();
    init();
  }

  /**
   * Constructor which accepts which RemoteBanksProvider implementation need to follow.
   * Currently there is only one RemoteBanksProvider implementation with Parallel Call Mechanisms
   * May be we can implement serial call mechanisms also so this constructor will give the option
   * define based on the caller
   * @param remoteBanksProvider
   * @throws Exception
   */
  public BanksRemoteCalls(RemoteBanksProvider remoteBanksProvider) throws Exception {
    this.remoteBanksProvider = remoteBanksProvider;
    init();
  }

  public static void init() throws Exception {
    config = new ObjectMapper()
        .readValue(Thread.currentThread().getContextClassLoader().getResource("banks-v2.json"),
            Map.class);
  }

  /**
   * Banks handle method which returns bank details by calling remote servers.
   * @param queryMap Query params in the request.
   * @return List of remote bank details
   */
  public List<BankDetails> handle(Map<String, String[]> queryMap) {
    Map<String, String> paramsMap = BanksUtil.getParamsMap(queryMap);
    List<BankModel> remoteBanksDetails = (List<BankModel>) remoteBanksProvider
        .getRemoteBanksDetails(new ArrayList<String>(config.values()));

    List<BankDetails> bankDetails = remoteBanksDetails.stream().map(BanksRemoteCalls::remoteBanksMapper)
        .collect(Collectors.toList());
    //Sorting the response from banks because call happening in parallel so can't ensure the order
    bankDetails.sort(Comparator.comparing(BankDetails::getId));
    if (!paramsMap.isEmpty()) {
      bankDetails = filterBanksResponse(bankDetails, paramsMap);
    }
    return BanksUtil.paginatedResponse(bankDetails, paramsMap);
  }

  /**
   * Filter bank details. This method will create the list of predicates based on the query params
   * and return the list of banks based on the filter
   * @param bankDetails
   * @param queryParamMap
   * @return
   */
  private static List<BankDetails> filterBanksResponse(List<BankDetails> bankDetails,
      Map<String, String> queryParamMap) {
    List<Predicate<BankDetails>> allPredicates = getCommonPredicates(
        queryParamMap);
    if (queryParamMap.containsKey("auth")) {
      allPredicates.add(bank -> queryParamMap.get("auth").equals(bank.getAuth()));
    }
    return bankDetails.stream().filter(allPredicates.stream().reduce(x -> true, Predicate::and))
        .collect(
            Collectors.toList());
  }

  private static BankDetails remoteBanksMapper(BankModel bankModel) {
    BankDetails bankDetails = new BankDetails();
    bankDetails.setName(bankModel.getName());
    bankDetails.setId(bankModel.getBic());
    bankDetails.setCountryCode(bankModel.getCountryCode());
    bankDetails.setAuth(bankModel.getAuth());
    return bankDetails;
  }

}
