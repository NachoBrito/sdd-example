package es.nachobrito.sdd.example.application.port.out;

import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Outbound port for persisting and retrieving order aggregates.
 */
public interface OrderRepositoryPort {

  /**
   * Persists a new order aggregate.
   *
   * @param order the order to persist
   * @return the persisted order
   */
  Order save(Order order);

  /**
   * Updates an existing order aggregate.
   *
   * @param order the order to update
   * @return the updated order
   */
  Order update(Order order);

  /**
   * Finds an order by its unique identifier.
   *
   * @param id the order identifier
   * @return an Optional containing the order if found
   */
  Optional<Order> findById(UUID id);

  /**
   * Checks if an order exists with the specified identifier.
   *
   * @param id the order identifier
   * @return true if the order exists
   */
  boolean existsById(UUID id);

  /**
   * Permanently deletes an order by its identifier.
   *
   * @param id the order identifier
   */
  void deleteById(UUID id);

  /**
   * Queries orders matching the specified filter criteria and sort options.
   *
   * @param status optional order status filter
   * @param customerEmail optional customer email filter
   * @param startDate optional start date filter
   * @param endDate optional end date filter
   * @param sortField the field to sort by (date, status)
   * @param direction the sort direction (asc, desc)
   * @return the list of matching orders
   */
  List<Order> findAll(
      @Nullable OrderStatus status,
      @Nullable String customerEmail,
      @Nullable Instant startDate,
      @Nullable Instant endDate,
      String sortField,
      String direction);
}

