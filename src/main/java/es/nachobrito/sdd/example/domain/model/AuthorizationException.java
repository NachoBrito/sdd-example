package es.nachobrito.sdd.example.domain.model;

/**
 * Thrown when an authenticated user lacks permission or ownership for the requested resource.
 */
public class AuthorizationException extends AuthException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new authorization exception with the error message.
   *
   * @param message the error description
   */
  public AuthorizationException(String message) {
    super(message);
  }
}

