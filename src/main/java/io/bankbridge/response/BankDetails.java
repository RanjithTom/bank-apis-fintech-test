package io.bankbridge.response;

import java.util.ArrayList;

/**
 * Common response model for remote and static Banks
 */
public class BankDetails {

  public String id;
  public String name;
  public String countryCode;
  public String auth;
  public ArrayList products;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getAuth() {
    return auth;
  }

  public void setAuth(String auth) {
    this.auth = auth;
  }

  public ArrayList getProducts() {
    return products;
  }

  public void setProducts(ArrayList products) {
    this.products = products;
  }
}
