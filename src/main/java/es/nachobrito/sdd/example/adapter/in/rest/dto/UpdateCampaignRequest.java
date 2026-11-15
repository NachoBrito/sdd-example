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

/**
 * REST request payload for updating a promotional campaign.
 *
 * @param name the updated campaign name
 * @param description the updated optional description
 * @param status the updated lifecycle status
 * @param startDate the updated activation timestamp in UTC
 * @param endDate the updated expiration timestamp in UTC
 * @param discountPercent the updated discount percentage from 0 to 100
 * @param predicates the replacement eligibility predicates
 */
@Serdeable
@Introspected
public record UpdateCampaignRequest(
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
  public UpdateCampaignRequest {
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

