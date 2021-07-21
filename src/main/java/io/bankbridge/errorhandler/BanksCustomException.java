package io.bankbridge.errorhandler;

public class BanksCustomException extends RuntimeException {

  public int getStatus() {
    return status;
  }

  private int status;

  public BanksCustomException(int status, String message) {
    super(message);
    this.status = status;
  }



}
