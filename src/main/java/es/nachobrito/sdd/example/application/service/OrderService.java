package es.nachobrito.sdd.example.application.service;

import es.nachobrito.sdd.example.application.port.in.OrderUseCase;
import es.nachobrito.sdd.example.application.port.out.OrderEventPublisherPort;
import es.nachobrito.sdd.example.application.port.out.OrderRepositoryPort;
import es.nachobrito.sdd.example.domain.model.DuplicateOrderIdentifierException;
import es.nachobrito.sdd.example.domain.model.InvalidOrderDataException;
import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderDeletionConflictException;
import es.nachobrito.sdd.example.domain.model.OrderNotFoundException;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Application service implementing the order use cases and publishing lifecycle events.
 */
@Singleton
public class OrderService implements OrderUseCase {

  private final OrderRepositoryPort repository;
  private final OrderEventPublisherPort eventPublisher;
  private final Clock clock;

  /**
   * Constructs the service with dependencies and default system UTC clock.
   *
   * @param repository the outbound repository port
   * @param eventPublisher the outbound event publisher port
   */
  @Inject
  public OrderService(OrderRepositoryPort repository, OrderEventPublisherPort eventPublisher) {
    this(repository, eventPublisher, Clock.systemUTC());
  }

  /**
   * Constructs the OrderService with repository, publisher, and clock dependencies.
   *
   * @param repository the outbound repository port
   * @param eventPublisher the outbound event publisher port
   * @param clock the system clock
   */
  public OrderService(
      OrderRepositoryPort repository,
      OrderEventPublisherPort eventPublisher,
      Clock clock) {
    this.repository = repository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  public Order createOrder(CreateOrderCommand command) {
    Objects.requireNonNull(command, "Command cannot be null");
    if (repository.existsById(command.id())) {
      throw new DuplicateOrderIdentifierException(command.id());
    }

    Instant now = clock.instant();
    Instant orderDate = command.date() != null ? command.date() : now;
    OrderStatus status = command.status() != null ? command.status() : OrderStatus.PENDING;

    long total = Order.calculateTotal(command.orderLines());
    Order order = new Order(
        command.id(),
        command.customerName(),
        command.customerEmail(),
        orderDate,
        status,
        total,
        command.currency(),
        command.deliveryAddress(),
        command.invoiceAddress(),
        command.orderLines(),
        now,
        now);

    Order saved = repository.save(order);
    eventPublisher.publishOrderCreated(saved);
    return saved;
  }

  @Override
  public Order getOrder(UUID id) {
    Objects.requireNonNull(id, "Order id cannot be null");
    return repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
  }

  @Override
  public Order updateOrder(UpdateOrderCommand command) {
    Objects.requireNonNull(command, "Command cannot be null");
    Order existing = getOrder(command.id());
    Instant now = clock.instant();

    Order updated = existing.withUpdatedAttributes(
        command.customerName(),
        command.customerEmail(),
        command.date(),
        command.status(),
        command.currency(),
        command.deliveryAddress(),
        command.invoiceAddress(),
        command.orderLines(),
        now);

    Order saved = repository.update(updated);
    if (saved.status() == OrderStatus.CANCELLED) {
      eventPublisher.publishOrderCancelled(saved);
    } else {
      eventPublisher.publishOrderUpdated(saved);
    }
    return saved;
  }

  @Override
  public void deleteOrder(UUID id) {
    Objects.requireNonNull(id, "Order id cannot be null");
    Order existing = getOrder(id);

    if (existing.status() != OrderStatus.PENDING && existing.status() != OrderStatus.CANCELLED) {
      throw new OrderDeletionConflictException(id, existing.status());
    }

    repository.deleteById(id);
    eventPublisher.publishOrderCancelled(existing);
  }

  @Override
  public List<Order> listOrders(ListOrdersQuery query) {
    Objects.requireNonNull(query, "Query cannot be null");
    Instant start = query.startDate();
    Instant end = query.endDate();
    if (start != null && end != null && start.isAfter(end)) {
      throw new InvalidOrderDataException("startDate cannot be after endDate");
    }

    return repository.findAll(
        query.status(),
        query.customerEmail(),
        start,
        end,
        query.sortField(),
        query.direction());
  }
}
