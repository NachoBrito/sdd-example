package es.nachobrito.sdd.example.application.port.out;

import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Outbound port for persisting and querying product records.
 */
public interface ProductRepositoryPort {

  /**
   * Persists a product in the repository.
   *
   * @param product the product to persist
   * @return the persisted product
   */
  Product save(Product product);

  /**
   * Finds a product by its unique identifier.
   *
   * @param id the product identifier
   * @return an Optional containing the product if found
   */
  Optional<Product> findById(UUID id);

  /**
   * Finds a product by its stock keeping unit.
   *
   * @param sku the stock keeping unit
   * @return an Optional containing the product if found
   */
  Optional<Product> findBySku(String sku);

  /**
   * Checks if a product exists with the specified identifier.
   *
   * @param id the product identifier
   * @return true if the product exists
   */
  boolean existsById(UUID id);

  /**
   * Checks if a product exists with the specified SKU.
   *
   * @param sku the stock keeping unit
   * @return true if a product with the SKU exists
   */
  boolean existsBySku(String sku);

  /**
   * Checks if a product with the specified SKU exists with a different identifier.
   *
   * @param sku the stock keeping unit
   * @param id the product identifier to exclude
   * @return true if another product has this SKU
   */
  boolean existsBySkuAndIdNot(String sku, UUID id);

  /**
   * Permanently deletes a product by identifier.
   *
   * @param id the product identifier
   */
  void deleteById(UUID id);

  /**
   * Searches and filters products matching the specified criteria.
   *
   * @param sku optional SKU filter
   * @param nameRegex optional regular expression for name matching
   * @param status optional lifecycle status filter
   * @param sortField the field to sort by (name, status, createdAt)
   * @param direction the sort direction (asc, desc)
   * @return the list of matching products
   */
  List<Product> findAll(
      @Nullable String sku,
      @Nullable String nameRegex,
      @Nullable ProductStatus status,
      String sortField,
      String direction);
}

