package es.nachobrito.sdd.example.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Aggregate root representing a promotional discount campaign.
 *
 * @param id the unique campaign identifier
 * @param name the campaign name
 * @param description the optional campaign description
 * @param status the lifecycle status
 * @param startDate the inclusive activation timestamp in UTC
 * @param endDate the inclusive expiration timestamp in UTC
 * @param discountPercent the integer discount percentage from 0 to 100
 * @param predicates the eligibility predicates combined with logical AND
 */
public record Campaign(
    UUID id,
    String name,
    @Nullable String description,
    CampaignStatus status,
    Instant startDate,
    Instant endDate,
    int discountPercent,
    List<CampaignPredicate> predicates) {

  public static final int MAX_NAME_LENGTH = 255;
  public static final int MAX_DESCRIPTION_LENGTH = 4000;
  public static final int MAX_DISCOUNT_PERCENT = 100;

  /**
   * Validates all campaign invariants.
   */
  public Campaign {
    if (id == null) {
      throw new InvalidCampaignDataException("Campaign id cannot be null");
    }
    if (name == null || name.isBlank()) {
      throw new InvalidCampaignDataException("Campaign name cannot be blank");
    }
    name = name.trim();
    if (name.length() > MAX_NAME_LENGTH) {
      throw new InvalidCampaignDataException(
          "Campaign name cannot exceed " + MAX_NAME_LENGTH + " characters");
    }
    if (description != null) {
      description = description.trim();
      if (description.length() > MAX_DESCRIPTION_LENGTH) {
        throw new InvalidCampaignDataException(
            "Campaign description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters");
      }
    }
    if (status == null) {
      throw new InvalidCampaignDataException("Campaign status cannot be null");
    }
    if (startDate == null) {
      throw new InvalidCampaignDataException("Campaign startDate cannot be null");
    }
    if (endDate == null) {
      throw new InvalidCampaignDataException("Campaign endDate cannot be null");
    }
    if (endDate.isBefore(startDate)) {
      throw new InvalidCampaignDataException("Campaign endDate cannot be before startDate");
    }
    if (discountPercent < 0 || discountPercent > MAX_DISCOUNT_PERCENT) {
      throw new InvalidCampaignDataException(
          "Campaign discountPercent must be between 0 and " + MAX_DISCOUNT_PERCENT);
    }
    if (predicates == null) {
      predicates = List.of();
    } else {
      for (CampaignPredicate predicate : predicates) {
        if (predicate == null) {
          throw new InvalidCampaignDataException("Campaign predicates cannot contain null");
        }
      }
      predicates = List.copyOf(predicates);
    }
  }

  /**
   * Returns an unmodifiable view of the campaign predicates.
   *
   * @return the campaign predicates
   */
  @Override
  public List<CampaignPredicate> predicates() {
    return Collections.unmodifiableList(predicates);
  }

  /**
   * Checks whether this campaign is active at the given timestamp.
   *
   * @param now the evaluation timestamp
   * @return true if status is ACTIVE and now is within [startDate, endDate]
   */
  public boolean isActiveAt(Instant now) {
    Objects.requireNonNull(now, "Timestamp cannot be null");
    return status == CampaignStatus.ACTIVE
        && !now.isBefore(startDate)
        && !now.isAfter(endDate);
  }

  /**
   * Evaluates whether a product matches all campaign predicates.
   *
   * @param product the target product
   * @return true if predicates is empty or every predicate matches the product
   */
  public boolean matches(Product product) {
    Objects.requireNonNull(product, "Product cannot be null");
    return predicates.stream().allMatch(predicate -> predicate.matches(product));
  }

  /**
   * Converts this campaign to a request-time ActiveDiscount projection.
   *
   * @return the active discount summary
   */
  public ActiveDiscount toActiveDiscount() {
    return new ActiveDiscount(id, name, discountPercent);
  }
}

