package io.bankbridge.handler;


import static org.mockito.Mockito.mock;

import io.bankbridge.model.BankModel;
import io.bankbridge.provider.RemoteBanksProvider;
import io.bankbridge.provider.RemoteBanksProviderImpl;
import io.bankbridge.response.BankDetails;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BanksRemoteCallsTest {

  private RemoteBanksProvider remoteBanksProvider;

  private BanksRemoteCalls banksRemoteCalls;

  @Before
  public void start() throws Exception {
    remoteBanksProvider = mock(RemoteBanksProvider.class);
    banksRemoteCalls = new BanksRemoteCalls(remoteBanksProvider);
  }

  @Test
  public void verify_SuccessResponseWithDefaultSize() {
    Mockito.when(remoteBanksProvider.getRemoteBanksDetails(Mockito.any())).thenReturn(getMockRemoteBanks());
    List<BankDetails> bankDetails = banksRemoteCalls.handle(new HashMap<>());
    Assert.assertEquals(bankDetails.size(), 3);
    Assert.assertEquals(bankDetails.get(0).getId(), "id1");

  }

  @Test
  public void verify_SuccessResponseWithDefinedSize() {
    Mockito.when(remoteBanksProvider.getRemoteBanksDetails(Mockito.any())).thenReturn(getMockRemoteBanks());

    String[] size = {"1"};
    Map<String, String[]> paramMap = Map.of("size", size);
    List<BankDetails> bankDetails = banksRemoteCalls.handle(paramMap);
    //If size is defined as 12 without any filters then it will return 12 banks details
    Assert.assertEquals(bankDetails.size(), 1);
  }

  @Test
  public void verify_SuccessResponseWithFilterCountrycode() {
    Mockito.when(remoteBanksProvider.getRemoteBanksDetails(Mockito.any())).thenReturn(getMockRemoteBanks());
    String[] countryCode = {"DE"};
    Map<String, String[]> paramMap = Map.of( "countrycode", countryCode);
    List<BankDetails> bankDetails = banksRemoteCalls.handle(paramMap);
    //in mock bank details there are 2 banks with countrycode
    Assert.assertEquals(bankDetails.size(), 2);
    Assert.assertTrue(bankDetails.stream().allMatch(bank -> bank.getCountryCode().equals("DE")));
  }


  private List<BankModel> getMockRemoteBanks() {
    List<BankModel> bankDetailsList = new ArrayList<>();
    BankModel bankDetails = new BankModel();
    bankDetails.setCountryCode("NO");
    bankDetails.setName("TestBankNO");
    bankDetails.setBic("id2");
    bankDetailsList.add(bankDetails);
    bankDetails = new BankModel();
    bankDetails.setCountryCode("DE");
    bankDetails.setName("TestBankEN");
    bankDetails.setBic("id1");
    bankDetailsList.add(bankDetails);
    bankDetails = new BankModel();
    bankDetails.setCountryCode("DE");
    bankDetails.setName("TestBankEN2");
    bankDetails.setBic("id3");
    bankDetailsList.add(bankDetails);
    return bankDetailsList;
  }
}