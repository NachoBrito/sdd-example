package es.nachobrito.sdd.example.domain.model;

import java.util.UUID;

/**
 * Signals that a campaign already uses the given identifier.
 */
public class DuplicateCampaignIdentifierException extends CampaignException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates the exception for the duplicate campaign identifier.
   *
   * @param id the duplicate campaign identifier
   */
  public DuplicateCampaignIdentifierException(UUID id) {
    super("Campaign already exists with identifier: " + id);
  }
}

