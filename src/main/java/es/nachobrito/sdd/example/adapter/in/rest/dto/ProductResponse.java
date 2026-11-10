package es.nachobrito.sdd.example.adapter.in.rest.dto;

import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductStatus;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * REST API response model representing product attributes and timestamps.
 *
 * @param id the unique product identifier
 * @param sku the stock keeping unit
 * @param name the product name
 * @param description the optional description
 * @param priceInCents the price in cents
 * @param currency the ISO currency code
 * @param status the lifecycle status
 * @param createdAt the creation timestamp
 * @param updatedAt the last update timestamp
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
    Instant createdAt,
    Instant updatedAt) {

  /**
   * Maps a domain Product aggregate to a ProductResponse DTO.
   *
   * @param product the domain product
   * @return the response DTO
   */
  public static ProductResponse fromDomain(Product product) {
    Objects.requireNonNull(product, "Product cannot be null");
    return new ProductResponse(
        product.id(),
        product.sku(),
        product.name(),
        product.description(),
        product.priceInCents(),
        product.currency(),
        product.status(),
        product.createdAt(),
        product.updatedAt());
  }
}
