package es.nachobrito.sdd.example.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.application.port.out.OrderRepositoryPort;
import es.nachobrito.sdd.example.domain.model.DeliveryAddress;
import es.nachobrito.sdd.example.domain.model.InvoiceAddress;
import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderDeletionConflictException;
import es.nachobrito.sdd.example.domain.model.OrderLine;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceDeleteTest {

  @Mock
  private OrderRepositoryPort repository;

  private OrderService service;
  private Instant now;

  private static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  private static DeliveryAddress sampleDelivery() {
    return new DeliveryAddress(
        "Alice Smith", "+1-555-123-4567", "123 Main St", "Apt 4B",
        "Springfield", "IL", "62701", "USA");
  }

  private static InvoiceAddress sampleInvoice() {
    return new InvoiceAddress(
        "Acme Corp", "US123456789", "+1-555-987-6543", "456 Market St", null,
        "Chicago", "IL", "60601", "USA");
  }

  private static List<OrderLine> sampleLines() {
    return List.of(new OrderLine(PRODUCT_ID, 2, 5_000));
  }

  @BeforeEach
  void setUp() {
    now = Instant.parse("2026-11-10T10:00:00Z");
    Clock fixedClock = Clock.fixed(now, ZoneOffset.UTC);
    service = new OrderService(repository, fixedClock);
  }

  @Test
  void deleteOrderThrowsConflictWhenPaid() {
    Order order = Order.create(
        ORDER_ID, "John Doe", "john@example.com", now, OrderStatus.PAID, "USD",
        sampleDelivery(), sampleInvoice(), sampleLines());

    when(repository.findById(ORDER_ID)).thenReturn(Optional.of(order));

    assertThrows(OrderDeletionConflictException.class, () -> service.deleteOrder(ORDER_ID));
  }

  @Test
  void deleteOrderThrowsConflictWhenShipped() {
    Order order = Order.create(
        ORDER_ID, "John Doe", "john@example.com", now, OrderStatus.SHIPPED, "USD",
        sampleDelivery(), sampleInvoice(), sampleLines());

    when(repository.findById(ORDER_ID)).thenReturn(Optional.of(order));

    assertThrows(OrderDeletionConflictException.class, () -> service.deleteOrder(ORDER_ID));
  }

  @Test
  void deleteOrderThrowsConflictWhenDelivered() {
    Order order = Order.create(
        ORDER_ID, "John Doe", "john@example.com", now, OrderStatus.DELIVERED, "USD",
        sampleDelivery(), sampleInvoice(), sampleLines());

    when(repository.findById(ORDER_ID)).thenReturn(Optional.of(order));

    assertThrows(OrderDeletionConflictException.class, () -> service.deleteOrder(ORDER_ID));
  }

  @Test
  void deleteOrderDeletesPendingOrder() {
    Order order = Order.create(
        ORDER_ID, "John Doe", "john@example.com", now, OrderStatus.PENDING, "USD",
        sampleDelivery(), sampleInvoice(), sampleLines());

    when(repository.findById(ORDER_ID)).thenReturn(Optional.of(order));

    service.deleteOrder(ORDER_ID);

    verify(repository).deleteById(ORDER_ID);
  }

  @Test
  void deleteOrderDeletesCancelledOrder() {
    Order order = Order.create(
        ORDER_ID, "John Doe", "john@example.com", now, OrderStatus.CANCELLED, "USD",
        sampleDelivery(), sampleInvoice(), sampleLines());

    when(repository.findById(ORDER_ID)).thenReturn(Optional.of(order));

    service.deleteOrder(ORDER_ID);

    verify(repository).deleteById(ORDER_ID);
  }
}
