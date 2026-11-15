package es.nachobrito.sdd.example.adapter.out.persistence;

import es.nachobrito.sdd.example.domain.model.Campaign;
import es.nachobrito.sdd.example.domain.model.CampaignPredicate;
import es.nachobrito.sdd.example.domain.model.CampaignStatus;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Persistence entity mapping the MariaDB campaigns table.
 *
 * @param id the primary key UUID
 * @param name the campaign name
 * @param description the optional campaign description
 * @param status the status string
 * @param startDate the inclusive activation timestamp
 * @param endDate the inclusive expiration timestamp
 * @param discountPercent the discount percentage
 */
@MappedEntity("campaigns")
public record CampaignEntity(
    @Id @NonNull UUID id,
    @NonNull String name,
    @Nullable String description,
    @NonNull String status,
    @NonNull @MappedProperty("start_date") Instant startDate,
    @NonNull @MappedProperty("end_date") Instant endDate,
    @MappedProperty("discount_percent") int discountPercent) {

  /**
   * Converts this persistence entity and its predicates to a domain Campaign.
   *
   * @param predicates the associated campaign predicates
   * @return the domain Campaign aggregate
   */
  public Campaign toDomain(List<CampaignPredicate> predicates) {
    return new Campaign(
        this.id,
        this.name,
        this.description,
        CampaignStatus.valueOf(this.status),
        this.startDate,
        this.endDate,
        this.discountPercent,
        predicates);
  }

  /**
   * Creates a persistence entity from a domain Campaign.
   *
   * @param domain the domain Campaign
   * @return the CampaignEntity record
   */
  public static CampaignEntity fromDomain(Campaign domain) {
    Objects.requireNonNull(domain, "Campaign cannot be null");
    return new CampaignEntity(
        domain.id(),
        domain.name(),
        domain.description(),
        domain.status().name(),
        domain.startDate(),
        domain.endDate(),
        domain.discountPercent());
  }
}

