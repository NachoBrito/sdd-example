package es.nachobrito.sdd.example.adapter.out.persistence;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.Sort;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Micronaut Data JDBC repository for OrderEntity.
 */
@JdbcRepository(dialect = Dialect.MYSQL)
public interface OrderJdbcRepository extends GenericRepository<OrderEntity, UUID> {

  /**
   * Finds an order entity by its unique identifier.
   *
   * @param id the order identifier
   * @return an Optional containing the entity if found
   */
  Optional<OrderEntity> findById(UUID id);

  /**
   * Checks if an order entity exists with the specified identifier.
   *
   * @param id the order identifier
   * @return true if the entity exists
   */
  boolean existsById(UUID id);

  /**
   * Deletes an order entity by its identifier.
   *
   * @param id the order identifier
   */
  void deleteById(UUID id);

  /**
   * Inserts a new order entity with a client-assigned identifier.
   *
   * @param entity the entity to insert
   * @return the inserted entity
   */
  OrderEntity insert(OrderEntity entity);

  /**
   * Updates an existing order entity.
   *
   * @param entity the entity to update
   * @return the updated entity
   */
  OrderEntity update(OrderEntity entity);

  /**
   * Searches and filters orders matching the given criteria with dynamic sorting.
   *
   * @param userId optional owning user identifier filter
   * @param status optional status filter
   * @param customerEmail optional customer email filter
   * @param startDate optional start date filter
   * @param endDate optional end date filter
   * @param sort dynamic sort order
   * @return the matching list of order entities
   */
  @Query(
      "SELECT * FROM orders order_entity_"
          + " WHERE (:userId IS NULL OR order_entity_.user_id = :userId)"
          + " AND (:status IS NULL OR :status = '' OR order_entity_.status = :status)"
          + " AND (:customerEmail IS NULL OR :customerEmail = ''"
          + " OR order_entity_.customer_email = :customerEmail)"
          + " AND (:startDate IS NULL OR order_entity_.order_date >= :startDate)"
          + " AND (:endDate IS NULL OR order_entity_.order_date <= :endDate)")
  List<OrderEntity> search(
      @Nullable UUID userId,
      @Nullable String status,
      @Nullable String customerEmail,
      @Nullable Instant startDate,
      @Nullable Instant endDate,
      Sort sort);
}

