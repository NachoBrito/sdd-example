package es.nachobrito.sdd.example.domain.model;

/**
 * Base exception for the category domain.
 */
public abstract class CategoryException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates a category exception with an error message.
   *
   * @param message the error message
   */
  protected CategoryException(String message) {
    super(message);
  }
}

