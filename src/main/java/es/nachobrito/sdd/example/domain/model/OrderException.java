package es.nachobrito.sdd.example.domain.model;

/**
 * Base abstract exception for all order domain errors.
 */
public abstract class OrderException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new order exception with the error message.
   *
   * @param message the error description
   */
  protected OrderException(String message) {
    super(message);
  }

  /**
   * Constructs a new order exception with the error message and cause.
   *
   * @param message the error description
   * @param cause the underlying cause
   */
  protected OrderException(String message, Throwable cause) {
    super(message, cause);
  }
}

