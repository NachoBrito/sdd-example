package es.nachobrito.sdd.example.domain.model;

/**
 * Thrown when user or token domain invariants fail validation.
 */
public class InvalidUserDataException extends AuthException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception with the validation message.
   *
   * @param message the validation error description
   */
  public InvalidUserDataException(String message) {
    super(message);
  }
}

