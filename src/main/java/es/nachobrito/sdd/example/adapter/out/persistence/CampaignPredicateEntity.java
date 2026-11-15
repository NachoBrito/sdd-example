package es.nachobrito.sdd.example.adapter.out.persistence;

import es.nachobrito.sdd.example.domain.model.CampaignPredicate;
import es.nachobrito.sdd.example.domain.model.PredicateType;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import java.util.Objects;
import java.util.UUID;

/**
 * Persistence entity mapping the MariaDB campaign_predicates table.
 *
 * @param id the primary key UUID
 * @param campaignId the parent campaign identifier
 * @param predicateType the predicate type string
 * @param predicateValue the predicate operand value
 */
@MappedEntity("campaign_predicates")
public record CampaignPredicateEntity(
    @Id @NonNull UUID id,
    @NonNull @MappedProperty("campaign_id") UUID campaignId,
    @NonNull @MappedProperty("predicate_type") String predicateType,
    @NonNull @MappedProperty("predicate_value") String predicateValue) {

  /**
   * Converts this entity to a domain CampaignPredicate.
   *
   * @return the domain CampaignPredicate
   */
  public CampaignPredicate toDomain() {
    return new CampaignPredicate(
        PredicateType.valueOf(this.predicateType),
        this.predicateValue);
  }

  /**
   * Creates a persistence entity from a domain CampaignPredicate.
   *
   * @param id the row identifier
   * @param campaignId the parent campaign identifier
   * @param domain the domain predicate
   * @return the CampaignPredicateEntity record
   */
  public static CampaignPredicateEntity fromDomain(
      UUID id, UUID campaignId, CampaignPredicate domain) {
    Objects.requireNonNull(id, "Predicate id cannot be null");
    Objects.requireNonNull(campaignId, "Campaign id cannot be null");
    Objects.requireNonNull(domain, "Predicate cannot be null");
    return new CampaignPredicateEntity(
        id,
        campaignId,
        domain.predicateType().name(),
        domain.value());
  }
}

