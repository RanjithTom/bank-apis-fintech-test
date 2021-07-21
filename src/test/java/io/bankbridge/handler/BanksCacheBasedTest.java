package io.bankbridge.handler;


import io.bankbridge.response.BankDetails;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BanksCacheBasedTest {

  @BeforeClass
  public static void setUp() throws Exception {
    BanksCacheBased.init();
  }

  @Test
  public void verify_SuccessResponseWithDefaultSize() {
    List<BankDetails> bankDetails = BanksCacheBased.handle(new HashMap<>());
    Assert.assertEquals(bankDetails.size(), 5);
    Assert.assertEquals(bankDetails.get(0).getId(), "DOLORENOR9XXX");
    Assert.assertEquals(bankDetails.get(0).getName(), "Bank Dolores");
    Assert.assertEquals(bankDetails.get(0).getCountryCode(), "NO");
  }

  @Test
  public void verify_SuccessResponseWithDefinedSize() {
    String[] size = {"12"};
    Map<String, String[]> paramMap = Map.of("size", size);
    List<BankDetails> bankDetails = BanksCacheBased.handle(paramMap);
    //If size is defined as 12 without any filters then it will return 12 banks details
    Assert.assertEquals(bankDetails.size(), 12);
  }

  @Test
  public void verify_SuccessResponseWithDefinedSizeAndFilter() {
    String[] size = {"12"};
    String[] countryCode = {"DE"};
    Map<String, String[]> paramMap = Map.of("size", size, "countrycode", countryCode);
    List<BankDetails> bankDetails = BanksCacheBased.handle(paramMap);
    //If size is defined as 12 with filter of country code DE, there is only 3 banks with countryCode DE
    Assert.assertEquals(bankDetails.size(), 3);
    Assert.assertTrue(bankDetails.stream().allMatch(bank -> bank.getCountryCode().equals("DE")));
  }

  @Test
  public void verify_SuccessResponseWithFilterForId() {
    String[] id = {"MOLLITNOR4XXX"};
    Map<String, String[]> paramMap = Map.of("id", id);
    List<BankDetails> bankDetails = BanksCacheBased.handle(paramMap);
    Assert.assertEquals(bankDetails.size(), 1);
    Assert.assertEquals(bankDetails.get(0).getId(), "MOLLITNOR4XXX");
    Assert.assertEquals(bankDetails.get(0).getName(), "Mbanken");
    Assert.assertEquals(bankDetails.get(0).getCountryCode(), "NO");
  }

  @Test
  public void verify_SuccessResponseWithFilterForName() {
    String[] name = {"Animat"};
    Map<String, String[]> paramMap = Map.of("name", name);
    List<BankDetails> bankDetails = BanksCacheBased.handle(paramMap);
    //There is only one bank with name Animat
    Assert.assertEquals(bankDetails.size(), 1);
    Assert.assertEquals(bankDetails.get(0).getId(), "ANIMDEU7XXX");
    Assert.assertEquals(bankDetails.get(0).getName(), "Animat");
    Assert.assertEquals(bankDetails.get(0).getCountryCode(), "DE");
  }

  @Test
  public void verify_SuccessResponseWithPagnationAndDefinedSize() {
    String[] size = {"19"};
    String[] pageNo = {"1"};
    Map<String, String[]> paramMap = Map.of("size", size, "pageNo", pageNo);
    List<BankDetails> bankDetails = BanksCacheBased.handle(paramMap);
    //There is only 20 banks in static so if the request for 1st pagination of size 19 then remaining only one bank
    Assert.assertEquals(bankDetails.size(), 1);
  }
}