package es.nachobrito.sdd.example.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.application.port.in.CreateOrderUseCase.CreateOrderCommand;
import es.nachobrito.sdd.example.application.port.in.UpdateOrderUseCase.UpdateOrderCommand;
import es.nachobrito.sdd.example.application.port.out.OrderRepositoryPort;
import es.nachobrito.sdd.example.domain.model.DeliveryAddress;
import es.nachobrito.sdd.example.domain.model.DuplicateOrderIdentifierException;
import es.nachobrito.sdd.example.domain.model.InvoiceAddress;
import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderLine;
import es.nachobrito.sdd.example.domain.model.OrderNotFoundException;
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
class OrderServiceCommandTest {

  @Mock
  private OrderRepositoryPort repository;

  private OrderService service;
  private Instant now;

  private static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final DeliveryAddress DELIVERY = new DeliveryAddress(
      "Alice Smith", "+1-555-123-4567", "123 Main St", "Apt 4B",
      "Springfield", "IL", "62701", "USA");
  private static final InvoiceAddress INVOICE = new InvoiceAddress(
      "Acme Corp", "US123456789", "+1-555-987-6543", "456 Market St", null,
      "Chicago", "IL", "60601", "USA");
  private static final List<OrderLine> LINES = List.of(new OrderLine(PRODUCT_ID, 2, 5_000));

  @BeforeEach
  void setUp() {
    now = Instant.parse("2026-11-10T10:00:00Z");
    Clock fixedClock = Clock.fixed(now, ZoneOffset.UTC);
    service = new OrderService(repository, fixedClock);
  }

  @Test
  void createOrderSavesAndReturnsOrder() {
    CreateOrderCommand command = new CreateOrderCommand(
        ORDER_ID, "John Doe", "john@example.com", null, null, "USD",
        DELIVERY, INVOICE, LINES);

    Order expected = Order.create(
        ORDER_ID, "John Doe", "john@example.com", now, OrderStatus.PENDING, "USD",
        DELIVERY, INVOICE, LINES);

    when(repository.existsById(ORDER_ID)).thenReturn(false);
    when(repository.save(any(Order.class))).thenReturn(expected);

    Order actual = service.createOrder(command);
    assertEquals(expected, actual);
  }

  @Test
  void createOrderDefaultsStatusToPending() {
    CreateOrderCommand command = new CreateOrderCommand(
        ORDER_ID, "John Doe", "john@example.com", null, null, "USD",
        DELIVERY, INVOICE, LINES);

    when(repository.existsById(ORDER_ID)).thenReturn(false);
    when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

    Order actual = service.createOrder(command);
    assertEquals(OrderStatus.PENDING, actual.status());
  }

  @Test
  void createOrderDefaultsDateToNow() {
    CreateOrderCommand command = new CreateOrderCommand(
        ORDER_ID, "John Doe", "john@example.com", null, null, "USD",
        DELIVERY, INVOICE, LINES);

    when(repository.existsById(ORDER_ID)).thenReturn(false);
    when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

    Order actual = service.createOrder(command);
    assertEquals(now, actual.date());
  }

  @Test
  void createOrderUsesExplicitDate() {
    Instant explicitDate = Instant.parse("2026-11-01T08:00:00Z");
    CreateOrderCommand command = new CreateOrderCommand(
        ORDER_ID, "John Doe", "john@example.com", explicitDate, OrderStatus.PAID, "USD",
        DELIVERY, INVOICE, LINES);

    when(repository.existsById(ORDER_ID)).thenReturn(false);
    when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

    Order actual = service.createOrder(command);
    assertEquals(explicitDate, actual.date());
  }

  @Test
  void createOrderUsesExplicitStatus() {
    CreateOrderCommand command = new CreateOrderCommand(
        ORDER_ID, "John Doe", "john@example.com", now, OrderStatus.PAID, "USD",
        DELIVERY, INVOICE, LINES);

    when(repository.existsById(ORDER_ID)).thenReturn(false);
    when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

    Order actual = service.createOrder(command);
    assertEquals(OrderStatus.PAID, actual.status());
  }

  @Test
  void createOrderRejectsDuplicateId() {
    CreateOrderCommand command = new CreateOrderCommand(
        ORDER_ID, "John Doe", "john@example.com", null, null, "USD",
        DELIVERY, INVOICE, LINES);

    when(repository.existsById(ORDER_ID)).thenReturn(true);

    assertThrows(DuplicateOrderIdentifierException.class, () -> service.createOrder(command));
  }

  @Test
  void updateOrderModifiesAttributes() {
    Order existing = Order.create(
        ORDER_ID, "John Doe", "john@example.com", now, OrderStatus.PENDING, "USD",
        DELIVERY, INVOICE, LINES);

    when(repository.findById(ORDER_ID)).thenReturn(Optional.of(existing));
    when(repository.update(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

    UpdateOrderCommand command = new UpdateOrderCommand(
        ORDER_ID, "Jane Doe", "jane@example.com", now, OrderStatus.PAID, "USD",
        DELIVERY, INVOICE, LINES);

    Order updated = service.updateOrder(command);
    assertEquals("Jane Doe", updated.customerName());
  }

  @Test
  void updateOrderThrowsWhenNotFound() {
    when(repository.findById(ORDER_ID)).thenReturn(Optional.empty());

    UpdateOrderCommand command = new UpdateOrderCommand(
        ORDER_ID, "Jane Doe", "jane@example.com", now, OrderStatus.PAID, "USD",
        DELIVERY, INVOICE, LINES);

    assertThrows(OrderNotFoundException.class, () -> service.updateOrder(command));
  }
}
