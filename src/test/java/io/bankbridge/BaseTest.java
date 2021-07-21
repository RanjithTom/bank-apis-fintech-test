package io.bankbridge;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.fail;
import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;


public class BaseTest {

  private static HttpClient client = HttpClient.newHttpClient();

  @BeforeClass
  public static void setUp() throws Exception {

      String[] args = {"8082"};
      Main.main(args);
      awaitInitialization();
  }

  @AfterClass
  public static void tearDown() {
    stop();
  }

  /**
   * Returns sample mock response for the Banks details and it will return same response for all banks request to MockServer.
   */
  public static void generateMockServer() {
    stubFor(get(urlPathMatching("/.*"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{\n" +
                "\"bic\":\"PARIATURDEU0XXX\",\n" +
                "\"name\":\"Banco de espiritu santo\",\n" +
                "\"countryCode\":\"GB\",\n" +
                "\"auth\":\"oauth\"\n" +
                "}")));
  }

  /**
   * Returns sample mock response for Royal Bank of Fun only rest of the banks will get 404.
   */
  public static void generateOneBankMatchMockResponse() {
    stubFor(any(urlPathEqualTo("/rbf"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{\n" +
                "\"bic\":\"DOLORENOR2XXX\",\n" +
                "\"name\":\"Royal Bank of Fun\",\n" +
                "\"countryCode\":\"GB\",\n" +
                "\"auth\":\"oauth\"\n" +
                "}")));
  }

  /**
   * Requesting the actual API with HttpClient and return the string represention of body
   * and response status
   * @param path API path including queryparams
   * @return
   */
  protected TestResponse request(String path) {
    try {

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create("http://localhost:8082" + path))
          .build();
      HttpResponse response = client.send(request, BodyHandlers.ofString());
      return new TestResponse(response.statusCode(), (String) response.body());
    } catch (InterruptedException | IOException e) {
      e.printStackTrace();
      fail("Sending request failed: " + e.getMessage());
      return null;
    }
  }

  /**
   * Sample POJO class for converting the response from server
   */
  protected static class TestResponse {
    public final String body;
    public final int status;
    public TestResponse(int status, String body) {
      this.status = status;
      this.body = body;
    }

  }

}
