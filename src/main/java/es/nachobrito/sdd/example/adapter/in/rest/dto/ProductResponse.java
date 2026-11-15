package es.nachobrito.sdd.example.adapter.in.rest.dto;

import es.nachobrito.sdd.example.domain.model.ActiveDiscount;
import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductStatus;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * REST API response model representing product attributes, timestamps, and active discounts.
 *
 * @param id the unique product identifier
 * @param sku the stock keeping unit
 * @param name the product name
 * @param description the optional description
 * @param priceInCents the price in cents
 * @param currency the ISO currency code
 * @param status the lifecycle status
 * @param categoryId the optional category identifier
 * @param createdAt the creation timestamp
 * @param updatedAt the last update timestamp
 * @param discounts the active promotional discounts resolved at request time
 */
@Serdeable
@Introspected
public record ProductResponse(
    UUID id,
    String sku,
    String name,
    @Nullable String description,
    int priceInCents,
    String currency,
    ProductStatus status,
    @Nullable UUID categoryId,
    Instant createdAt,
    Instant updatedAt,
    List<ProductDiscountResponse> discounts) {

  /**
   * Defensively copies discounts to enforce immutability.
   */
  public ProductResponse {
    discounts = discounts == null ? List.of() : List.copyOf(discounts);
  }

  /**
   * Returns an unmodifiable view of the discounts list.
   *
   * @return the active discount responses
   */
  @Override
  public List<ProductDiscountResponse> discounts() {
    return Collections.unmodifiableList(discounts);
  }

  /**
   * Maps a domain Product aggregate without active discounts to a ProductResponse DTO.
   *
   * @param product the domain product
   * @return the response DTO
   */
  public static ProductResponse fromDomain(Product product) {
    return fromDomain(product, List.of());
  }

  /**
   * Maps a domain Product aggregate and active discounts to a ProductResponse DTO.
   *
   * @param product the domain product
   * @param activeDiscounts the active discounts matching the product
   * @return the response DTO
   */
  public static ProductResponse fromDomain(
      Product product, List<ActiveDiscount> activeDiscounts) {
    Objects.requireNonNull(product, "Product cannot be null");
    Objects.requireNonNull(activeDiscounts, "Active discounts cannot be null");
    List<ProductDiscountResponse> discountResponses = activeDiscounts.stream()
        .map(ProductDiscountResponse::fromDomain)
        .toList();
    return new ProductResponse(
        product.id(),
        product.sku(),
        product.name(),
        product.description(),
        product.priceInCents(),
        product.currency(),
        product.status(),
        product.categoryId(),
        product.createdAt(),
        product.updatedAt(),
        discountResponses);
  }

  /**
   * Maps a list of products and resolved active discounts to response DTOs.
   *
   * @param products the domain products
   * @param discountsByProduct active discounts keyed by product identifier
   * @return the list of response DTOs
   */
  public static List<ProductResponse> fromDomainList(
      List<Product> products, Map<UUID, List<ActiveDiscount>> discountsByProduct) {
    Objects.requireNonNull(products, "Products cannot be null");
    Objects.requireNonNull(discountsByProduct, "Discounts map cannot be null");
    return products.stream()
        .map(product ->
            fromDomain(product, discountsByProduct.getOrDefault(product.id(), List.of())))
        .toList();
  }
}
