package es.nachobrito.sdd.example.adapter.in.rest.dto;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.nachobrito.sdd.example.domain.model.DeliveryAddress;
import es.nachobrito.sdd.example.domain.model.InvoiceAddress;
import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderLine;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderResponseTest {

  private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID PROD_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");

  @Test
  void fromDomainMapsOrderFields() {
    Order order = sampleOrder();
    OrderResponse response = OrderResponse.fromDomain(order);

    assertAll(
        () -> assertEquals(ID, response.id()),
        () -> assertEquals("John Doe", response.customerName()),
        () -> assertEquals("john@example.com", response.customerEmail()),
        () -> assertEquals(NOW, response.date()),
        () -> assertEquals(OrderStatus.PENDING, response.status()),
        () -> assertEquals(5_000L, response.totalAmountInCents()),
        () -> assertEquals("EUR", response.currency()),
        () -> assertEquals(1, response.orderLines().size()));
  }

  @Test
  void orderLinesReturnsUnmodifiableList() {
    Order order = sampleOrder();
    OrderResponse response = OrderResponse.fromDomain(order);
    List<OrderLineResponse> lines = response.orderLines();
    OrderLineResponse newLine = new OrderLineResponse(PROD_ID, 1, 100, 100);

    assertThrows(UnsupportedOperationException.class, () -> lines.add(newLine));
  }

  @Test
  void orderLinesContainsMappedElements() {
    Order order = sampleOrder();
    OrderResponse response = OrderResponse.fromDomain(order);

    assertEquals(1, response.orderLines().size());
  }

  @Test
  void nullOrderLinesDefaultsToEmptyList() {
    OrderResponse response = new OrderResponse(
        ID, "Customer", "c@example.com", NOW, OrderStatus.PENDING, 0, "USD",
        null, null, null, NOW, NOW);

    assertEquals(List.of(), response.orderLines());
  }

  private static Order sampleOrder() {
    DeliveryAddress delivery = new DeliveryAddress(
        "John Doe", "123456789", "Main St 1", "Apt 2", "City", "State", "12345", "Country");
    InvoiceAddress invoice = new InvoiceAddress(
        "Company Inc", "VAT123", "987654321", "Market St 5", null, "City", "State",
        "12345", "Country");
    OrderLine line = new OrderLine(PROD_ID, 2, 2_500);
    return new Order(
        ID, "John Doe", "john@example.com", NOW, OrderStatus.PENDING, 5_000, "EUR",
        delivery, invoice, List.of(line), NOW, NOW);
  }
}

