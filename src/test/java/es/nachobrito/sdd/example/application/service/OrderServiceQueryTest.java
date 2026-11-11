package es.nachobrito.sdd.example.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.application.port.in.ListOrdersUseCase.ListOrdersQuery;
import es.nachobrito.sdd.example.application.port.out.OrderRepositoryPort;
import es.nachobrito.sdd.example.domain.model.DeliveryAddress;
import es.nachobrito.sdd.example.domain.model.InvalidOrderDataException;
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
class OrderServiceQueryTest {

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
    return List.of(new OrderLine(PRODUCT_ID, 2, 5000));
  }

  @BeforeEach
  void setUp() {
    now = Instant.parse("2026-11-10T10:00:00Z");
    Clock fixedClock = Clock.fixed(now, ZoneOffset.UTC);
    service = new OrderService(repository, fixedClock);
  }

  @Test
  void getOrderReturnsExistingOrder() {
    Order order = Order.create(
        ORDER_ID, "John Doe", "john@example.com", now, OrderStatus.PENDING, "USD",
        sampleDelivery(), sampleInvoice(), sampleLines());

    when(repository.findById(ORDER_ID)).thenReturn(Optional.of(order));

    Order actual = service.getOrder(ORDER_ID);
    assertEquals(order, actual);
  }

  @Test
  void getOrderThrowsWhenNotFound() {
    when(repository.findById(ORDER_ID)).thenReturn(Optional.empty());

    assertThrows(OrderNotFoundException.class, () -> service.getOrder(ORDER_ID));
  }

  @Test
  void listOrdersPassesParametersToRepository() {
    Instant start = Instant.parse("2026-11-01T00:00:00Z");
    Instant end = Instant.parse("2026-11-10T23:59:59Z");
    ListOrdersQuery query = new ListOrdersQuery(
        OrderStatus.PENDING, "john@example.com", start, end, "date", "asc");

    Order order = Order.create(
        ORDER_ID, "John Doe", "john@example.com", now, OrderStatus.PENDING, "USD",
        sampleDelivery(), sampleInvoice(), sampleLines());

    when(repository.findAll(OrderStatus.PENDING, "john@example.com", start, end, "date", "asc"))
        .thenReturn(List.of(order));

    List<Order> actual = service.listOrders(query);
    assertEquals(List.of(order), actual);
  }

  @Test
  void listOrdersRejectsStartDateAfterEndDate() {
    Instant start = Instant.parse("2026-11-10T00:00:00Z");
    Instant end = Instant.parse("2026-11-01T00:00:00Z");
    ListOrdersQuery query = new ListOrdersQuery(
        OrderStatus.PENDING, "john@example.com", start, end, "date", "asc");

    assertThrows(InvalidOrderDataException.class, () -> service.listOrders(query));
  }

  @Test
  void listOrdersAllowsNullStartDateWithNonNullEndDate() {
    Instant end = Instant.parse("2026-11-10T23:59:59Z");
    ListOrdersQuery query = new ListOrdersQuery(null, null, null, end, "date", "asc");
    when(repository.findAll(null, null, null, end, "date", "asc")).thenReturn(List.of());

    assertEquals(List.of(), service.listOrders(query));
  }

  @Test
  void listOrdersAllowsNonNullStartDateWithNullEndDate() {
    Instant start = Instant.parse("2026-11-01T00:00:00Z");
    ListOrdersQuery query = new ListOrdersQuery(null, null, start, null, "date", "asc");
    when(repository.findAll(null, null, start, null, "date", "asc")).thenReturn(List.of());

    assertEquals(List.of(), service.listOrders(query));
  }
}

