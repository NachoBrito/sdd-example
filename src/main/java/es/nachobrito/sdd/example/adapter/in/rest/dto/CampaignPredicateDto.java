package es.nachobrito.sdd.example.adapter.in.rest.dto;

import es.nachobrito.sdd.example.domain.model.CampaignPredicate;
import es.nachobrito.sdd.example.domain.model.PredicateType;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Objects;

/**
 * REST representation of a campaign eligibility predicate.
 *
 * @param predicateType the predicate operator
 * @param value the predicate operand value
 */
@Serdeable
@Introspected
public record CampaignPredicateDto(
    @NotNull PredicateType predicateType,
    @NotBlank @Size(max = 255) String value) {

  /**
   * Converts this DTO to a domain CampaignPredicate.
   *
   * @return the domain CampaignPredicate
   */
  public CampaignPredicate toDomain() {
    return new CampaignPredicate(predicateType, value);
  }

  /**
   * Maps a domain CampaignPredicate to a CampaignPredicateDto.
   *
   * @param predicate the domain predicate
   * @return the REST predicate DTO
   */
  public static CampaignPredicateDto fromDomain(CampaignPredicate predicate) {
    Objects.requireNonNull(predicate, "Predicate cannot be null");
    return new CampaignPredicateDto(predicate.predicateType(), predicate.value());
  }
}

