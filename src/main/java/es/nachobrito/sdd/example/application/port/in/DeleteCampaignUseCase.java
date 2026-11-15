package es.nachobrito.sdd.example.application.port.in;

import java.util.UUID;

/**
 * Inbound port for deleting a promotional campaign.
 */
@FunctionalInterface
public interface DeleteCampaignUseCase {

  /**
   * Permanently deletes a campaign by identifier.
   *
   * @param id the campaign identifier
   */
  void deleteCampaign(UUID id);
}

