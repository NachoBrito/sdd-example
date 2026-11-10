package es.nachobrito.sdd.example.domain.model;

import java.time.Instant;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Aggregate root representing a product in the catalog.
 *
 * @param id the unique product identifier
 * @param sku the stock keeping unit
 * @param name the human-readable product name
 * @param description the optional product description
 * @param priceInCents the price in integer cents
 * @param currency the ISO-4217 three-letter currency code
 * @param status the product lifecycle status
 * @param createdAt the creation timestamp in UTC
 * @param updatedAt the last update timestamp in UTC
 */
public record Product(
    UUID id,
    String sku,
    String name,
    @Nullable String description,
    int priceInCents,
    String currency,
    ProductStatus status,
    Instant createdAt,
    Instant updatedAt) {

  private static final int MAX_SKU_LENGTH = 100;
  private static final int MAX_NAME_LENGTH = 255;
  private static final int MAX_DESCRIPTION_LENGTH = 4000;

  /**
   * Compact constructor that validates all domain invariants.
   */
  public Product {
    if (id == null) {
      throw new InvalidProductDataException("Product id cannot be null");
    }
    if (sku == null || sku.isBlank()) {
      throw new InvalidProductDataException("Product sku cannot be blank");
    }
    sku = sku.trim();
    if (sku.length() > MAX_SKU_LENGTH) {
      throw new InvalidProductDataException(
          "Product sku cannot exceed " + MAX_SKU_LENGTH + " characters");
    }
    if (name == null || name.isBlank()) {
      throw new InvalidProductDataException("Product name cannot be blank");
    }
    name = name.trim();
    if (name.length() > MAX_NAME_LENGTH) {
      throw new InvalidProductDataException(
          "Product name cannot exceed " + MAX_NAME_LENGTH + " characters");
    }
    if (description != null) {
      description = description.trim();
      if (description.length() > MAX_DESCRIPTION_LENGTH) {
        throw new InvalidProductDataException(
            "Product description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters");
      }
    }
    if (priceInCents < 0) {
      throw new InvalidProductDataException(
          "Product priceInCents must be non-negative: " + priceInCents);
    }
    if (currency == null || currency.isBlank()) {
      throw new InvalidProductDataException("Product currency cannot be blank");
    }
    currency = currency.trim().toUpperCase(java.util.Locale.ROOT);
    validateCurrency(currency);
    if (status == null) {
      throw new InvalidProductDataException("Product status cannot be null");
    }
    if (createdAt == null) {
      throw new InvalidProductDataException("Product createdAt cannot be null");
    }
    if (updatedAt == null) {
      throw new InvalidProductDataException("Product updatedAt cannot be null");
    }
  }

  /**
   * Creates a new Product with the specified attributes and timestamps.
   *
   * @param id the unique product identifier
   * @param sku the stock keeping unit
   * @param name the product name
   * @param description the optional description
   * @param priceInCents the price in cents
   * @param currency the ISO-4217 currency code
   * @param status the product status
   * @param timestamp the creation and update timestamp
   * @return the new Product instance
   */
  public static Product create(
      UUID id,
      String sku,
      String name,
      @Nullable String description,
      int priceInCents,
      String currency,
      ProductStatus status,
      Instant timestamp) {
    Objects.requireNonNull(timestamp, "Timestamp cannot be null");
    return new Product(
        id, sku, name, description, priceInCents, currency, status, timestamp, timestamp);
  }

  /**
   * Returns a new Product instance with updated attributes and refreshed timestamp.
   *
   * @param newSku the updated stock keeping unit
   * @param newName the updated product name
   * @param newDescription the updated description
   * @param newPriceInCents the updated price in cents
   * @param newCurrency the updated currency code
   * @param newStatus the updated product status
   * @param newUpdatedAt the new update timestamp
   * @return the updated Product instance
   */
  public Product withUpdatedAttributes(
      String newSku,
      String newName,
      @Nullable String newDescription,
      int newPriceInCents,
      String newCurrency,
      ProductStatus newStatus,
      Instant newUpdatedAt) {
    return new Product(
        this.id,
        newSku,
        newName,
        newDescription,
        newPriceInCents,
        newCurrency,
        newStatus,
        this.createdAt,
        newUpdatedAt);
  }

  private static void validateCurrency(String currencyCode) {
    try {
      Currency.getInstance(currencyCode);
    } catch (IllegalArgumentException e) {
      throw new InvalidProductDataException("Invalid currency code: " + currencyCode, e);
    }
  }
}

