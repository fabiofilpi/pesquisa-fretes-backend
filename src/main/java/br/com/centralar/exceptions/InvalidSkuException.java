package br.com.centralar.exceptions;

public class InvalidSkuException extends IllegalArgumentException {
  public InvalidSkuException(final String message) {
    super(message);
  }
}
