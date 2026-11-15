package es.nachobrito.sdd.example.application.port.in;

import es.nachobrito.sdd.example.domain.model.Campaign;
import es.nachobrito.sdd.example.domain.model.CampaignStatus;
import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Inbound port for listing and filtering promotional campaigns.
 */
@FunctionalInterface
public interface ListCampaignsUseCase {

  /**
   * Retrieves campaigns matching optional filters.
   *
   * @param query the campaign filter parameters
   * @return the list of matching campaigns
   */
  List<Campaign> listCampaigns(ListCampaignsQuery query);

  /**
   * Query model for filtering campaigns.
   *
   * @param name optional case-insensitive name substring filter
   * @param description optional case-insensitive description substring filter
   * @param status optional lifecycle status filter
   * @param startDate optional lower overlap bound in UTC
   * @param endDate optional upper overlap bound in UTC
   */
  record ListCampaignsQuery(
      @Nullable String name,
      @Nullable String description,
      @Nullable CampaignStatus status,
      @Nullable Instant startDate,
      @Nullable Instant endDate) {}
}

