package es.nachobrito.sdd.example.domain.model;

/**
 * Base abstract exception for all authentication and user domain errors.
 */
public abstract class AuthException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new auth exception with the error message.
   *
   * @param message the error description
   */
  protected AuthException(String message) {
    super(message);
  }

  /**
   * Constructs a new auth exception with the error message and cause.
   *
   * @param message the error description
   * @param cause the underlying cause
   */
  protected AuthException(String message, Throwable cause) {
    super(message, cause);
  }
}

