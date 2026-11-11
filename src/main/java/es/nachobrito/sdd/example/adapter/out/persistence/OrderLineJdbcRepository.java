package es.nachobrito.sdd.example.adapter.out.persistence;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;
import java.util.List;
import java.util.UUID;

/**
 * Micronaut Data JDBC repository for OrderLineEntity.
 */
@JdbcRepository(dialect = Dialect.MYSQL)
public interface OrderLineJdbcRepository extends GenericRepository<OrderLineEntity, UUID> {

  /**
   * Finds all order line entities for a given order identifier.
   *
   * @param orderId the parent order identifier
   * @return the list of order line entities
   */
  List<OrderLineEntity> findByOrderId(UUID orderId);

  /**
   * Deletes all order line entities for a given order identifier.
   *
   * @param orderId the parent order identifier
   */
  void deleteByOrderId(UUID orderId);

  /**
   * Inserts an order line entity.
   *
   * @param entity the entity to insert
   * @return the inserted entity
   */
  OrderLineEntity insert(OrderLineEntity entity);
}

