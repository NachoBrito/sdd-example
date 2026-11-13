package es.nachobrito.sdd.example.application.port.in;

/**
 * Inbound port for authenticating a user with username and password.
 */
@FunctionalInterface
public interface LoginUseCase {

  /**
   * Authenticates user credentials and issues JWT tokens.
   *
   * @param command the login command
   * @return the issued token pair
   */
  IssuedTokens login(LoginCommand command);

  /**
   * Command payload for credential login.
   *
   * @param username the username
   * @param password the plaintext password
   */
  record LoginCommand(String username, String password) {}

  /**
   * Result containing issued access and refresh JWT tokens.
   *
   * @param accessToken the signed access JWT
   * @param refreshToken the signed refresh JWT
   * @param expiresInSeconds the access token lifetime in seconds
   */
  record IssuedTokens(String accessToken, String refreshToken, long expiresInSeconds) {}
}

