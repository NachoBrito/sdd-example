package es.nachobrito.sdd.example.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderLinesValidationTest {

  private static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");
  private static final DeliveryAddress DELIVERY = new DeliveryAddress(
      "Alice Smith", "+1-555-123-4567", "123 Main St", "Apt 4B",
      "Springfield", "IL", "62701", "USA");
  private static final InvoiceAddress INVOICE = new InvoiceAddress(
      "Acme Corp", "US123456789", "+1-555-987-6543", "456 Market St", null,
      "Chicago", "IL", "60601", "USA");
  private static final List<OrderLine> LINES = List.of(new OrderLine(PRODUCT_ID, 2, 5_000));

  @Test
  void blankCurrencyThrowsException() {
    assertThrows(
        InvalidOrderDataException.class,
        () -> Order.create(
            ORDER_ID, "John", "john@example.com", NOW, OrderStatus.PENDING, "   ",
            DELIVERY, INVOICE, LINES));
  }

  @Test
  void nullDeliveryAddressThrowsException() {
    assertThrows(
        InvalidOrderDataException.class,
        () -> Order.create(
            ORDER_ID, "John", "john@example.com", NOW, OrderStatus.PENDING, "USD",
            null, INVOICE, LINES));
  }

  @Test
  void nullInvoiceAddressThrowsException() {
    assertThrows(
        InvalidOrderDataException.class,
        () -> Order.create(
            ORDER_ID, "John", "john@example.com", NOW, OrderStatus.PENDING, "USD",
            DELIVERY, null, LINES));
  }

  @Test
  void emptyLinesThrowsException() {
    assertThrows(
        InvalidOrderDataException.class,
        () -> Order.create(
            ORDER_ID, "John", "john@example.com", NOW, OrderStatus.PENDING, "USD",
            DELIVERY, INVOICE, List.of()));
  }

  @Test
  void orderLineZeroQuantityThrowsException() {
    assertThrows(
        InvalidOrderDataException.class,
        () -> new OrderLine(PRODUCT_ID, 0, 100));
  }

  @Test
  void orderLineNegativeQuantityThrowsException() {
    assertThrows(
        InvalidOrderDataException.class,
        () -> new OrderLine(PRODUCT_ID, -1, 100));
  }

  @Test
  void orderLineZeroPriceIsValid() {
    OrderLine line = new OrderLine(PRODUCT_ID, 2, 0);
    assertEquals(0L, line.lineTotalInCents());
  }

  @Test
  void orderLineNegativePriceThrowsException() {
    assertThrows(
        InvalidOrderDataException.class,
        () -> new OrderLine(PRODUCT_ID, 1, -100));
  }
}

