package es.nachobrito.sdd.example.domain.model;

/**
 * Thrown when product attributes violate domain model validation rules.
 */
public class InvalidProductDataException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception with a descriptive error message.
   *
   * @param message the validation error description
   */
  public InvalidProductDataException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with an error message and underlying cause.
   *
   * @param message the validation error description
   * @param cause the underlying cause
   */
  public InvalidProductDataException(String message, Throwable cause) {
    super(message, cause);
  }
}

