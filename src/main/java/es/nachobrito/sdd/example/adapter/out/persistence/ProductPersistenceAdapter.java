package es.nachobrito.sdd.example.adapter.out.persistence;

import es.nachobrito.sdd.example.application.port.out.ProductRepositoryPort;
import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductStatus;
import io.micronaut.data.model.Sort;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Persistence adapter implementing the ProductRepositoryPort using Micronaut Data JDBC.
 */
@Singleton
public class ProductPersistenceAdapter implements ProductRepositoryPort {

  private final ProductJdbcRepository repository;

  /**
   * Constructs the persistence adapter with the JDBC repository.
   *
   * @param repository the Micronaut Data JDBC repository
   */
  public ProductPersistenceAdapter(ProductJdbcRepository repository) {
    this.repository = repository;
  }

  @Override
  public Product save(Product product) {
    Objects.requireNonNull(product, "Product cannot be null");
    ProductEntity entity = ProductEntity.fromDomain(product);
    ProductEntity saved;
    if (repository.existsById(product.id())) {
      saved = repository.update(entity);
    } else {
      saved = repository.insert(entity);
    }
    return saved.toDomain();
  }

  @Override
  public Optional<Product> findById(UUID id) {
    Objects.requireNonNull(id, "Product id cannot be null");
    return repository.findById(id).map(ProductEntity::toDomain);
  }

  @Override
  public Optional<Product> findBySku(String sku) {
    Objects.requireNonNull(sku, "Product sku cannot be null");
    return repository.findBySku(sku).map(ProductEntity::toDomain);
  }

  @Override
  public boolean existsById(UUID id) {
    Objects.requireNonNull(id, "Product id cannot be null");
    return repository.existsById(id);
  }

  @Override
  public boolean existsBySku(String sku) {
    Objects.requireNonNull(sku, "Product sku cannot be null");
    return repository.existsBySku(sku);
  }

  @Override
  public boolean existsBySkuAndIdNot(String sku, UUID id) {
    Objects.requireNonNull(sku, "Product sku cannot be null");
    Objects.requireNonNull(id, "Product id cannot be null");
    return repository.existsBySkuAndIdNot(sku, id);
  }

  @Override
  public void deleteById(UUID id) {
    Objects.requireNonNull(id, "Product id cannot be null");
    repository.deleteById(id);
  }

  @Override
  public List<Product> findAll(
      @Nullable String sku,
      @Nullable String nameRegex,
      @Nullable ProductStatus status,
      String sortField,
      String direction) {
    Sort sort = resolveSort(sortField, direction);
    String statusStr = status == null ? null : status.name();
    return repository.search(sku, nameRegex, statusStr, sort).stream()
        .map(ProductEntity::toDomain)
        .toList();
  }

  private static Sort resolveSort(String sortField, String direction) {
    String normalized = sortField == null
        ? "name"
        : sortField.trim().toLowerCase(java.util.Locale.ROOT);
    String property = switch (normalized) {
      case "status" -> "status";
      case "createdat", "created_at" -> "createdAt";
      default -> "name";
    };
    boolean isDesc = "desc".equalsIgnoreCase(direction);
    return isDesc ? Sort.of(Sort.Order.desc(property)) : Sort.of(Sort.Order.asc(property));
  }
}
