package es.nachobrito.sdd.example.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents one eligibility predicate on a promotional campaign.
 *
 * @param predicateType the predicate operator
 * @param value the predicate operand value
 */
public record CampaignPredicate(PredicateType predicateType, String value) {

  public static final int MAX_VALUE_LENGTH = 255;

  /**
   * Validates all predicate invariants.
   */
  public CampaignPredicate {
    if (predicateType == null) {
      throw new InvalidCampaignDataException("Campaign predicateType cannot be null");
    }
    if (value == null || value.isBlank()) {
      throw new InvalidCampaignDataException("Campaign predicate value cannot be blank");
    }
    value = value.trim();
    if (value.length() > MAX_VALUE_LENGTH) {
      throw new InvalidCampaignDataException(
          "Campaign predicate value cannot exceed " + MAX_VALUE_LENGTH + " characters");
    }
    if (predicateType == PredicateType.IN_CATEGORY
        || predicateType == PredicateType.NOT_IN_CATEGORY) {
      validateCategoryUuid(value);
    } else {
      validateNonNegativeCents(value);
    }
  }

  /**
   * Evaluates whether a product matches this predicate.
   *
   * @param product the target product
   * @return true if the product matches the predicate
   */
  public boolean matches(Product product) {
    Objects.requireNonNull(product, "Product cannot be null");
    UUID productCategoryId = product.categoryId();
    return switch (predicateType) {
      case IN_CATEGORY ->
          productCategoryId != null && productCategoryId.equals(UUID.fromString(value));
      case NOT_IN_CATEGORY ->
          productCategoryId == null || !productCategoryId.equals(UUID.fromString(value));
      case IS_CHEAPER_THAN -> product.priceInCents() < Long.parseLong(value);
      case IS_MORE_EXPENSIVE_THAN -> product.priceInCents() > Long.parseLong(value);
    };
  }

  private static void validateCategoryUuid(String candidate) {
    try {
      UUID.fromString(candidate);
    } catch (IllegalArgumentException e) {
      throw new InvalidCampaignDataException(
          "Category predicate value must be a valid UUID: " + candidate, e);
    }
  }

  private static void validateNonNegativeCents(String candidate) {
    try {
      long cents = Long.parseLong(candidate);
      if (cents < 0) {
        throw new InvalidCampaignDataException(
            "Price predicate value must be non-negative: " + candidate);
      }
    } catch (NumberFormatException e) {
      throw new InvalidCampaignDataException(
          "Price predicate value must be a valid integer amount in cents: " + candidate, e);
    }
  }
}

