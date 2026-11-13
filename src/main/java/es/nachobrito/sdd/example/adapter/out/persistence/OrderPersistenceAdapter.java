package es.nachobrito.sdd.example.adapter.out.persistence;

import es.nachobrito.sdd.example.application.port.out.OrderRepositoryPort;
import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderLine;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import io.micronaut.data.model.Sort;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Persistence adapter implementing the OrderRepositoryPort with Micronaut Data JDBC.
 */
@Singleton
public class OrderPersistenceAdapter implements OrderRepositoryPort {

  private final OrderJdbcRepository orderRepository;
  private final OrderLineJdbcRepository orderLineRepository;

  /**
   * Constructs the persistence adapter with order and order line repositories.
   *
   * @param orderRepository the order repository
   * @param orderLineRepository the order line repository
   */
  public OrderPersistenceAdapter(
      OrderJdbcRepository orderRepository,
      OrderLineJdbcRepository orderLineRepository) {
    this.orderRepository = orderRepository;
    this.orderLineRepository = orderLineRepository;
  }

  @Override
  @Transactional
  public Order save(Order order) {
    Objects.requireNonNull(order, "Order cannot be null");
    OrderEntity entity = OrderEntity.fromDomain(order);
    orderRepository.insert(entity);
    insertOrderLines(order.id(), order.orderLines());
    return order;
  }

  @Override
  @Transactional
  public Order update(Order order) {
    Objects.requireNonNull(order, "Order cannot be null");
    OrderEntity entity = OrderEntity.fromDomain(order);
    orderRepository.update(entity);
    orderLineRepository.deleteByOrderId(order.id());
    insertOrderLines(order.id(), order.orderLines());
    return order;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Order> findById(UUID id) {
    Objects.requireNonNull(id, "Order id cannot be null");
    return orderRepository.findById(id).map(this::loadOrderWithLines);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsById(UUID id) {
    Objects.requireNonNull(id, "Order id cannot be null");
    return orderRepository.existsById(id);
  }

  @Override
  @Transactional
  public void deleteById(UUID id) {
    Objects.requireNonNull(id, "Order id cannot be null");
    orderLineRepository.deleteByOrderId(id);
    orderRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Order> findAll(
      @Nullable UUID userId,
      @Nullable OrderStatus status,
      @Nullable String customerEmail,
      @Nullable Instant startDate,
      @Nullable Instant endDate,
      String sortField,
      String direction) {
    Sort sort = resolveSort(sortField, direction);
    String statusStr = status == null ? null : status.name();
    return orderRepository
        .search(userId, statusStr, customerEmail, startDate, endDate, sort)
        .stream()
        .map(this::loadOrderWithLines)
        .toList();
  }

  private void insertOrderLines(UUID orderId, List<OrderLine> lines) {
    for (OrderLine line : lines) {
      UUID lineId = UUID.randomUUID();
      orderLineRepository.insert(OrderLineEntity.fromDomain(lineId, orderId, line));
    }
  }

  private Order loadOrderWithLines(OrderEntity entity) {
    List<OrderLine> lines = orderLineRepository.findByOrderId(entity.id()).stream()
        .map(OrderLineEntity::toDomain)
        .toList();
    return entity.toDomain(lines);
  }

  private static Sort resolveSort(String sortField, String direction) {
    String normalized = sortField == null
        ? "orderDate"
        : sortField.trim().toLowerCase(Locale.ROOT);
    String property = "status".equals(normalized) ? "status" : "orderDate";
    boolean isDesc = "desc".equalsIgnoreCase(direction);
    return isDesc ? Sort.of(Sort.Order.desc(property)) : Sort.of(Sort.Order.asc(property));
  }
}
