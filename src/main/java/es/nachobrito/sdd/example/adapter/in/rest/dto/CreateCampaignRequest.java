package es.nachobrito.sdd.example.adapter.in.rest.dto;

import es.nachobrito.sdd.example.domain.model.CampaignStatus;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * REST request payload for creating a promotional campaign.
 *
 * @param id the client-generated campaign identifier
 * @param name the campaign name
 * @param description the optional campaign description
 * @param status the lifecycle status
 * @param startDate the inclusive activation timestamp in UTC
 * @param endDate the inclusive expiration timestamp in UTC
 * @param discountPercent the discount percentage from 0 to 100
 * @param predicates the optional eligibility predicates
 */
@Serdeable
@Introspected
public record CreateCampaignRequest(
    @NotNull UUID id,
    @NotBlank @Size(max = 255) String name,
    @Nullable @Size(max = 4000) String description,
    @NotNull CampaignStatus status,
    @NotNull Instant startDate,
    @NotNull Instant endDate,
    @NotNull @Min(0) @Max(100) Integer discountPercent,
    @Nullable List<@NotNull @Valid CampaignPredicateDto> predicates) {

  /**
   * Defensively copies predicates to preserve immutability while retaining bean validation.
   */
  public CreateCampaignRequest {
    predicates =
        predicates == null
            ? List.of()
            : Collections.unmodifiableList(new ArrayList<>(predicates));
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
}

