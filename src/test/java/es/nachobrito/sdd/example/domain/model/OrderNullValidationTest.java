package es.nachobrito.sdd.example.domain.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderNullValidationTest {

  private static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");

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

  @Test
  void nullCustomerNameThrowsException() {
    assertThrows(InvalidOrderDataException.class, () -> Order.create(
        ORDER_ID, null, "john@example.com", NOW, OrderStatus.PENDING, "USD",
        sampleDelivery(), sampleInvoice(), sampleLines()));
  }

  @Test
  void nullCustomerEmailThrowsException() {
    assertThrows(InvalidOrderDataException.class, () -> Order.create(
        ORDER_ID, "John", null, NOW, OrderStatus.PENDING, "USD",
        sampleDelivery(), sampleInvoice(), sampleLines()));
  }

  @Test
  void nullCurrencyThrowsException() {
    assertThrows(InvalidOrderDataException.class, () -> Order.create(
        ORDER_ID, "John", "john@example.com", NOW, OrderStatus.PENDING, null,
        sampleDelivery(), sampleInvoice(), sampleLines()));
  }

  @Test
  void nullOrderLinesThrowsException() {
    assertThrows(InvalidOrderDataException.class, () -> Order.create(
        ORDER_ID, "John", "john@example.com", NOW, OrderStatus.PENDING, "USD",
        sampleDelivery(), sampleInvoice(), null));
  }

  @Test
  void nullCreatedAtThrowsException() {
    assertThrows(InvalidOrderDataException.class, () -> new Order(
        ORDER_ID, "John", "john@example.com", NOW, OrderStatus.PENDING, 10_000, "USD",
        sampleDelivery(), sampleInvoice(), sampleLines(), null, NOW));
  }

  @Test
  void nullUpdatedAtThrowsException() {
    assertThrows(InvalidOrderDataException.class, () -> new Order(
        ORDER_ID, "John", "john@example.com", NOW, OrderStatus.PENDING, 10_000, "USD",
        sampleDelivery(), sampleInvoice(), sampleLines(), NOW, null));
  }

  @Test
  void orderLineNullProductIdThrowsException() {
    assertThrows(InvalidOrderDataException.class, () -> new OrderLine(null, 1, 100));
  }
}
