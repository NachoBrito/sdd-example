package es.nachobrito.sdd.example.domain.model;

/**
 * Signals that campaign attributes violate domain validation rules.
 */
public class InvalidCampaignDataException extends CampaignException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates the exception with a validation error message.
   *
   * @param message the validation error description
   */
  public InvalidCampaignDataException(String message) {
    super(message);
  }

  /**
   * Creates the exception with a validation error message and cause.
   *
   * @param message the validation error description
   * @param cause the root cause
   */
  public InvalidCampaignDataException(String message, Throwable cause) {
    super(message, cause);
  }
}

