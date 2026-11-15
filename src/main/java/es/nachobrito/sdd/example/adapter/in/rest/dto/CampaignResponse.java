package es.nachobrito.sdd.example.adapter.in.rest.dto;

import es.nachobrito.sdd.example.domain.model.Campaign;
import es.nachobrito.sdd.example.domain.model.CampaignStatus;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * REST response representation of a promotional campaign.
 *
 * @param id the campaign identifier
 * @param name the campaign name
 * @param description the optional campaign description
 * @param status the lifecycle status
 * @param startDate the inclusive activation timestamp in UTC
 * @param endDate the inclusive expiration timestamp in UTC
 * @param discountPercent the integer discount percentage from 0 to 100
 * @param predicates the campaign eligibility predicates
 */
@Serdeable
@Introspected
public record CampaignResponse(
    UUID id,
    String name,
    @Nullable String description,
    CampaignStatus status,
    Instant startDate,
    Instant endDate,
    int discountPercent,
    List<CampaignPredicateDto> predicates) {

  /**
   * Defensively copies predicates to enforce immutability.
   */
  public CampaignResponse {
    predicates = predicates == null ? List.of() : List.copyOf(predicates);
  }

  /**
   * Returns an unmodifiable view of the predicates list.
   *
   * @return the predicates list
   */
  @Override
  public List<CampaignPredicateDto> predicates() {
    return Collections.unmodifiableList(predicates);
  }

  /**
   * Maps a domain Campaign record to a CampaignResponse DTO.
   *
   * @param campaign the domain campaign
   * @return the REST campaign response
   */
  public static CampaignResponse fromDomain(Campaign campaign) {
    Objects.requireNonNull(campaign, "Campaign cannot be null");
    List<CampaignPredicateDto> predicateDtos = campaign.predicates().stream()
        .map(CampaignPredicateDto::fromDomain)
        .toList();
    return new CampaignResponse(
        campaign.id(),
        campaign.name(),
        campaign.description(),
        campaign.status(),
        campaign.startDate(),
        campaign.endDate(),
        campaign.discountPercent(),
        predicateDtos);
  }
}

