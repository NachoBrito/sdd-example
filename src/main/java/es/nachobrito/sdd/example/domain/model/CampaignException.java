package es.nachobrito.sdd.example.domain.model;

/**
 * Base exception for the campaign domain.
 */
public abstract class CampaignException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates a campaign exception with an error message.
   *
   * @param message the error message
   */
  protected CampaignException(String message) {
    super(message);
  }

  /**
   * Creates a campaign exception with an error message and cause.
   *
   * @param message the error message
   * @param cause the root cause
   */
  protected CampaignException(String message, Throwable cause) {
    super(message, cause);
  }
}

