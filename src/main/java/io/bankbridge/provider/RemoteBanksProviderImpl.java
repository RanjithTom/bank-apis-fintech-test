package io.bankbridge.provider;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.bankbridge.errorhandler.BanksCustomException;
import io.bankbridge.model.BankModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


public class RemoteBanksProviderImpl implements RemoteBanksProvider{

  private static final Logger log = LoggerFactory.getLogger(RemoteBanksProviderImpl.class);

  private static HttpClient httpClient = HttpClient.newHttpClient();

  /**
   * Method will return the remote banks details by calling the remote bank endpoints.
   * @param uris
   * @return
   */
  public Object getRemoteBanksDetails(List<String> uris) {
    List<URI> urlDetails = new ArrayList<>();
    uris.forEach(uri -> urlDetails.add(URI.create(uri)));
    List<HttpRequest> requests = urlDetails.stream()
        .map(HttpRequest::newBuilder)
        .map(reqBuilder -> reqBuilder.build())
        .collect(toList());

    CompletableFuture completableFuture = getCompletableFuture(requests);
    try {
      return completableFuture.get();
    } catch (InterruptedException | ExecutionException exception) {
      log.error("Error in Parallel execution {}", exception.getMessage());
      throw new BanksCustomException(500, "Remote connection failed");
    }

  }

  /**
   * Asynchronous call implementation steps.
   * Method will return completable future after completing all remote banks calls by using
   * CompletableFuture.allOf mechanism.
   * It uses Java HttpClient Async call mechanisms with exceptionally. And the calls will not stop the execution
   * if there is any error in particular bank also if we get any error from remote banks also.
   * Currently it simply log the error and return null response for that bank. In final we are converting the response into
   * BankDetail by using objectmapper.
   * @param requests
   * @return
   */
  private CompletableFuture getCompletableFuture(List<HttpRequest> requests) {
    //Create list of CompletableFuture by using HttpClient.sendAsync
    List<CompletableFuture<String>> completableFutures =
        requests.stream()
            .map(request -> httpClient.sendAsync(request, BodyHandlers.ofString())
                .thenApply(resp -> {
                  var status = resp.statusCode();
                  //If there are scenarios if we didn't get the success response from bank need to handle it separately,
                  // because in last we are trying to map this into BankDetail, currently simply logging.
                  if (status != 200) {
                    log.error("Not able to fetch details successfully for Bank {} with response {}", resp.uri(),
                        resp.body());
                    return null;
                  } else {
                    return resp.body();
                  }
                }).exceptionally(ex -> {
                  //When particular bank is down it will not stop the entire execution, Also if needed we can raise event or something to track this
                  log.error("Error in Remote Connection with Bank {} with error {}", request.uri().toString(), ex.getMessage());
                  return null;
                })).collect(
            Collectors.toList());
    //Returns a new CompletableFuture that is completed when all of the given CompletableFutures complete.
    CompletableFuture<Void> allFutures = CompletableFuture
        .allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));

    //CompletableFuture.join() to get List of Banks details
    CompletableFuture<List<String>> allCompletableFuture = allFutures
        .thenApply(future -> completableFutures.stream()
            .map(completableFuture -> completableFuture.join())
            .collect(Collectors.toList()));
    //Map the response into BankModel class.
    CompletableFuture completableFuture = allCompletableFuture.thenApply(banks -> banks.stream()
        .filter(Objects::nonNull)
        .map(bank -> {
          try {
            return new ObjectMapper().readValue(bank, BankModel.class);
          } catch (IOException ioException) {
            log.error("Error in Mapping the Banks response model {}", ioException.getMessage());
            throw new BanksCustomException(500, "System not able to process the request");
          }
        }).filter(Objects::nonNull).collect(Collectors.toList()));
    return completableFuture;
  }
}
