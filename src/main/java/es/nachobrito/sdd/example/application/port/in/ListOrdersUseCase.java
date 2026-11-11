package es.nachobrito.sdd.example.application.port.in;

import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Inbound port for searching, filtering, and sorting orders.
 */
@FunctionalInterface
public interface ListOrdersUseCase {

  /**
   * Retrieves orders matching the filter query.
   *
   * @param query the search and sort query parameters
   * @return the list of matching orders
   */
  List<Order> listOrders(ListOrdersQuery query);

  /**
   * Query model for order search and filtering.
   *
   * @param status optional status filter
   * @param customerEmail optional customer email filter
   * @param startDate optional start date filter
   * @param endDate optional end date filter
   * @param sortField the sort field (date, status)
   * @param direction the sort direction (asc, desc)
   */
  record ListOrdersQuery(
      @Nullable OrderStatus status,
      @Nullable String customerEmail,
      @Nullable Instant startDate,
      @Nullable Instant endDate,
      String sortField,
      String direction) {}
}

