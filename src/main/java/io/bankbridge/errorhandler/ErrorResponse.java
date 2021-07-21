package io.bankbridge.errorhandler;

public class ErrorResponse {

  public String message;

  public ErrorResponse(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
