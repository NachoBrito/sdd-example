package es.nachobrito.sdd.example.domain.model;

/**
 * Thrown when order data violates domain invariants.
 */
public class InvalidOrderDataException extends OrderException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception with a message.
   *
   * @param message the validation error description
   */
  public InvalidOrderDataException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with a message and cause.
   *
   * @param message the validation error description
   * @param cause the underlying cause
   */
  public InvalidOrderDataException(String message, Throwable cause) {
    super(message, cause);
  }
}

