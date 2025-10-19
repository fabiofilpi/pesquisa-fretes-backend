package br.com.centralar.dtos;

public class AuthResponse {
  public String message;
  public long expiresIn;

  public AuthResponse() {}

  public AuthResponse(String message, long expiresIn) {
    this.message = message;
    this.expiresIn = expiresIn;
  }
}
