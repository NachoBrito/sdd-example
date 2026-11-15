package es.nachobrito.sdd.example.domain.model;

import java.util.UUID;

/**
 * Signals that a requested campaign does not exist.
 */
public class CampaignNotFoundException extends CampaignException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates the exception for the missing campaign identifier.
   *
   * @param id the missing campaign identifier
   */
  public CampaignNotFoundException(UUID id) {
    super("Campaign not found with identifier: " + id);
  }
}

