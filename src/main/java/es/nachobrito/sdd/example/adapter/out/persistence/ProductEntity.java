package es.nachobrito.sdd.example.adapter.out.persistence;

import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductStatus;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import java.time.Instant;
import java.util.UUID;

/**
 * Persistence entity mapping the MariaDB products table.
 *
 * @param id the primary key UUID
 * @param sku the unique stock keeping unit
 * @param name the product name
 * @param description the optional description
 * @param priceInCents the price in cents
 * @param currency the ISO currency code
 * @param status the status string
 * @param createdAt the creation timestamp
 * @param updatedAt the update timestamp
 */
@MappedEntity("products")
public record ProductEntity(
    @Id @NonNull UUID id,
    @NonNull String sku,
    @NonNull String name,
    @Nullable String description,
    @MappedProperty("price_in_cents") int priceInCents,
    @NonNull String currency,
    @NonNull String status,
    @NonNull @MappedProperty("created_at") Instant createdAt,
    @NonNull @MappedProperty("updated_at") Instant updatedAt) {

  /**
   * Converts this persistence entity to a domain Product.
   *
   * @return the domain Product instance
   */
  public Product toDomain() {
    return new Product(
        this.id,
        this.sku,
        this.name,
        this.description,
        this.priceInCents,
        this.currency,
        ProductStatus.valueOf(this.status),
        this.createdAt,
        this.updatedAt);
  }

  /**
   * Creates a persistence entity from a domain Product.
   *
   * @param domain the domain Product instance
   * @return the persistence entity
   */
  public static ProductEntity fromDomain(Product domain) {
    return new ProductEntity(
        domain.id(),
        domain.sku(),
        domain.name(),
        domain.description(),
        domain.priceInCents(),
        domain.currency(),
        domain.status().name(),
        domain.createdAt(),
        domain.updatedAt());
  }
}

