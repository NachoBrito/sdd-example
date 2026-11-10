package es.nachobrito.sdd.example.adapter.out.persistence;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.Sort;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Micronaut Data JDBC repository for the ProductEntity.
 */
@JdbcRepository(dialect = Dialect.MYSQL)
public interface ProductJdbcRepository extends GenericRepository<ProductEntity, UUID> {

  /**
   * Finds an entity by its identifier.
   *
   * @param id the product identifier
   * @return an Optional containing the entity if found
   */
  Optional<ProductEntity> findById(UUID id);

  /**
   * Checks if an entity exists with the specified identifier.
   *
   * @param id the product identifier
   * @return true if an entity exists
   */
  boolean existsById(UUID id);

  /**
   * Deletes an entity by its identifier.
   *
   * @param id the product identifier
   */
  void deleteById(UUID id);

  /**
   * Finds an entity by its stock keeping unit.
   *
   * @param sku the stock keeping unit
   * @return an Optional containing the entity if found
   */
  Optional<ProductEntity> findBySku(String sku);

  /**
   * Checks if an entity exists with the specified SKU.
   *
   * @param sku the stock keeping unit
   * @return true if an entity exists
   */
  boolean existsBySku(String sku);

  /**
   * Checks if an entity exists with the specified SKU and a different identifier.
   *
   * @param sku the stock keeping unit
   * @param id the identifier to exclude
   * @return true if another entity exists with the SKU
   */
  boolean existsBySkuAndIdNot(String sku, UUID id);

  /**
   * Inserts a new entity with a client-assigned identifier.
   *
   * @param entity the entity to insert
   * @return the inserted entity
   */
  ProductEntity insert(ProductEntity entity);

  /**
   * Updates an existing entity.
   *
   * @param entity the entity to update
   * @return the updated entity
   */
  ProductEntity update(ProductEntity entity);

  /**
   * Queries products by SKU, name regular expression, and status, with dynamic sorting.
   *
   * @param sku optional SKU filter
   * @param nameRegex optional regular expression for name matching
   * @param status optional status filter
   * @param sort dynamic sort order
   * @return the matching list of entities
   */
  @Query(
      "SELECT * FROM products product_entity_"
          + " WHERE (:sku IS NULL OR :sku = '' OR product_entity_.sku = :sku)"
          + " AND (:status IS NULL OR :status = '' OR product_entity_.status = :status)"
          + " AND (:nameRegex IS NULL OR :nameRegex = ''"
          + " OR product_entity_.name REGEXP :nameRegex)")
  List<ProductEntity> search(
      @Nullable String sku,
      @Nullable String nameRegex,
      @Nullable String status,
      Sort sort);
}
