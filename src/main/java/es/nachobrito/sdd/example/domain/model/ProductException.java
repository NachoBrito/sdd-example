package es.nachobrito.sdd.example.domain.model;

/**
 * Base abstract exception for all product domain errors.
 */
public abstract class ProductException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new product exception with the error message.
   *
   * @param message the error description
   */
  protected ProductException(String message) {
    super(message);
  }

  /**
   * Constructs a new product exception with the error message and cause.
   *
   * @param message the error description
   * @param cause the underlying cause
   */
  protected ProductException(String message, Throwable cause) {
    super(message, cause);
  }
}

