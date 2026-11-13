package es.nachobrito.sdd.example.domain.model;

/**
 * Thrown when user credentials or authentication tokens are invalid, expired, or revoked.
 */
public class AuthenticationException extends AuthException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new authentication exception with the error message.
   *
   * @param message the error description
   */
  public AuthenticationException(String message) {
    super(message);
  }

  /**
   * Constructs a new authentication exception with the error message and cause.
   *
   * @param message the error description
   * @param cause the underlying cause
   */
  public AuthenticationException(String message, Throwable cause) {
    super(message, cause);
  }
}

