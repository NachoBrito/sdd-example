package es.nachobrito.sdd.example.domain.model;

/**
 * Signals that category attributes violate domain validation rules.
 */
public class InvalidCategoryDataException extends CategoryException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates the exception with a validation error message.
   *
   * @param message the validation error description
   */
  public InvalidCategoryDataException(String message) {
    super(message);
  }
}

