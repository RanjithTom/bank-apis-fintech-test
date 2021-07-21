package io.bankbridge.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.bankbridge.BaseTest;
import io.bankbridge.response.BankDetails;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.List;

/**
 * Since this is lightweight component i just created integrations tests to check the entire flow.
 * In this it will start the server on port 8082 and also used wiremock stub to respond the RemoteBanksServer calls.
 */
@RunWith(JUnit4.class)
public class BanksIntegrationTest extends BaseTest {

  /**
   * Wiremock to respond for MockBanksServer
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(1234);

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final String BASE_REMOTE_ALL_BANKS_URL = "/v2/banks/all";
  private static final String BASE_STATIC_ALL_BANKS_URL = "/v1/banks/all";



  @Test
  public void testRemoteBanks_withSuccess_withDefaultSize() throws IOException {
    generateMockServer();
    TestResponse response = request(BASE_REMOTE_ALL_BANKS_URL);
    List<BankDetails> bankDetails = mapper.readValue(response.body, List.class);
    Assert.assertEquals(bankDetails.size(), 5);
    Assert.assertEquals(response.status, 200);
  }

  @Test
  public void testRemoteBanks_withSuccess_withDefinedSize() throws IOException {
    generateMockServer();
    TestResponse response = request(BASE_REMOTE_ALL_BANKS_URL+"?size=10");
    List<BankDetails> bankDetails = mapper.readValue(response.body, List.class);
    Assert.assertEquals(bankDetails.size(), 10);
    Assert.assertEquals(response.status, 200);
  }

  /**
   * Only one bank returns success then it will return one bank details only without failing
   * @throws IOException
   */
  @Test
  public void testRemoteBanks_WithOnlyOneBankServerUp_andRestFails() throws IOException {
    generateOneBankMatchMockResponse();
    TestResponse response = request(BASE_REMOTE_ALL_BANKS_URL);
    List<BankDetails> bankDetails = mapper.readValue(response.body, new TypeReference<List<BankDetails>>(){});
    Assert.assertEquals(bankDetails.size(), 1);
    Assert.assertEquals(bankDetails.get(0).getId(), "DOLORENOR2XXX");
    Assert.assertEquals(response.status, 200);
  }

  /**
   *Test working as expected but will take much time due to server not available
   * */
  @Test
  public void testRemoteBanks_WithErrorFromBanks() throws IOException {
    wireMockRule.stop();
    TestResponse response = request(BASE_REMOTE_ALL_BANKS_URL);
    List<BankDetails> bankDetails = mapper.readValue(response.body, new TypeReference<List<BankDetails>>(){});
    Assert.assertEquals(bankDetails.size(), 0);
  }

  @Test
  public void testRemoteBanks_withInvalidPaginationQueryParam() {
    TestResponse response = request(BASE_REMOTE_ALL_BANKS_URL+"?pageNo=invalidnumber");
    Assert.assertEquals(response.status, 400);
    Assert.assertTrue(response.body.contains("pageNo and size should be integer values!"));
  }

  @Test
  public void testRemoteBanks_withNegativePageNo() {
    TestResponse response = request(BASE_REMOTE_ALL_BANKS_URL+"?pageNo=-1");
    Assert.assertEquals(response.status, 400);
    Assert.assertTrue(response.body.contains("Page number is not valid!"));
  }

  @Test
  public void testRemoteBanks_withNegativePageSize() {
    TestResponse response = request(BASE_REMOTE_ALL_BANKS_URL+"?size=-1");
    Assert.assertEquals(response.status, 400);
    Assert.assertTrue(response.body.contains("Page size is not valid!"));
  }

  @Test
  public void testBanks_withNotFoundError() {
    TestResponse response = request("/v3/banks/all");
    Assert.assertEquals(response.status, 404);
    Assert.assertTrue(response.body.contains("Requested resource not found in the system"));
  }

  @Test
  public void testStaticBanks_success_withDefaultSize() throws IOException {
    TestResponse response = request(BASE_STATIC_ALL_BANKS_URL);
    List<BankDetails> bankDetails = mapper.readValue(response.body, new TypeReference<List<BankDetails>>(){});
    Assert.assertEquals(bankDetails.size(), 5);
    BankDetails bankDetail = bankDetails.get(0);
    Assert.assertEquals(bankDetail.getId(), "DOLORENOR9XXX");
    Assert.assertEquals(bankDetail.getName(), "Bank Dolores");
    Assert.assertEquals(bankDetail.getCountryCode(), "NO");
    Assert.assertEquals(response.status, 200);
  }

  @Test
  public void testStaticBanks_success_withFilterCountryCode() throws IOException {
    TestResponse response = request(BASE_STATIC_ALL_BANKS_URL+"?countrycode=GB&id=DOLORENOR2XXX");
    List<BankDetails> bankDetails = mapper.readValue(response.body, new TypeReference<List<BankDetails>>(){});
    Assert.assertEquals(bankDetails.size(), 1);
    Assert.assertEquals(bankDetails.get(0).getCountryCode(),"GB");
    Assert.assertEquals(bankDetails.get(0).getId(), "DOLORENOR2XXX");
    Assert.assertEquals(response.status, 200);
  }

  @Test
  public void testStaticBanks_success_withFilterName() throws IOException {
    TestResponse response = request(BASE_STATIC_ALL_BANKS_URL+"?name=Mbanken");
    List<BankDetails> bankDetails = mapper.readValue(response.body, new TypeReference<List<BankDetails>>(){});
    Assert.assertEquals(bankDetails.size(), 2);
    Assert.assertTrue(bankDetails.stream().allMatch(banks -> banks.getName().equals("Mbanken")));
    Assert.assertEquals(response.status, 200);
  }

  @Test
  public void testStaticBanks_success_withFilterId() throws IOException {
    TestResponse response = request(BASE_STATIC_ALL_BANKS_URL+"?id=MOLLITNOR4XXX");
    List<BankDetails> bankDetails = mapper.readValue(response.body, new TypeReference<List<BankDetails>>(){});
    Assert.assertEquals(bankDetails.size(), 1);
    Assert.assertTrue(bankDetails.stream().allMatch(banks -> banks.getId().equals("MOLLITNOR4XXX")));
    Assert.assertEquals(response.status, 200);
  }

  @Test
  public void testStaticBanks_success_withFilterProduct() throws IOException {
    TestResponse response = request(BASE_STATIC_ALL_BANKS_URL+"?product=accounts");
    List<BankDetails> bankDetails = mapper.readValue(response.body, new TypeReference<List<BankDetails>>(){});
    Assert.assertEquals(bankDetails.size(), 5);
    Assert.assertTrue(bankDetails.stream().allMatch(banks -> banks.getProducts().contains("accounts")));
    Assert.assertEquals(response.status, 200);
  }

  @Test
  public void testStaticBanks_success_withDefinedSize() throws IOException {
    TestResponse response = request(BASE_STATIC_ALL_BANKS_URL +"?size=10");
    List bankDetails = mapper.readValue(response.body, List.class);
    Assert.assertEquals(bankDetails.size(), 10);
    Assert.assertEquals(response.status, 200);
  }

  @Test
  public void testStaticBanks_withInvalidPaginationQueryParam() {
    TestResponse response = request(BASE_STATIC_ALL_BANKS_URL +"?pageNo=invalidnumber");
    Assert.assertEquals(response.status, 400);
    Assert.assertTrue(response.body.contains("pageNo and size should be integer values!"));
  }

  @Test
  public void testStaticBanks_Error_withNegativePageNo() {
    TestResponse response = request(BASE_STATIC_ALL_BANKS_URL +"?pageNo=-1");
    Assert.assertEquals(response.status, 400);
    Assert.assertTrue(response.body.contains("Page number is not valid!"));
  }

  @Test
  public void testStaticBanks_Error_withNegativePageSize() {
    TestResponse response = request(BASE_STATIC_ALL_BANKS_URL +"?size=-1");
    Assert.assertEquals(response.status, 400);
    Assert.assertTrue(response.body.contains("Page size is not valid!"));
  }

}