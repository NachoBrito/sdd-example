package es.nachobrito.sdd.example.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderValidationTest {

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
  void nullIdThrowsException() {
    assertThrows(
        InvalidOrderDataException.class,
        () -> Order.create(
            null, "John", "john@example.com", NOW, OrderStatus.PENDING, "USD",
            DELIVERY, INVOICE, LINES));
  }

  @Test
  void blankCustomerNameThrowsException() {
    assertThrows(
        InvalidOrderDataException.class,
        () -> Order.create(
            ORDER_ID, "   ", "john@example.com", NOW, OrderStatus.PENDING, "USD",
            DELIVERY, INVOICE, LINES));
  }

  @Test
  void overlongCustomerNameThrowsException() {
    String longName = "A".repeat(256);
    assertThrows(
        InvalidOrderDataException.class,
        () -> Order.create(
            ORDER_ID, longName, "john@example.com", NOW, OrderStatus.PENDING, "USD",
            DELIVERY, INVOICE, LINES));
  }

  @Test
  void exactMaxLengthCustomerNameIsValid() {
    String maxName = "A".repeat(255);
    Order order = Order.create(
        ORDER_ID, maxName, "john@example.com", NOW, OrderStatus.PENDING, "USD",
        DELIVERY, INVOICE, LINES);
    assertEquals(maxName, order.customerName());
  }

  @Test
  void invalidEmailThrowsException() {
    assertThrows(
        InvalidOrderDataException.class,
        () -> Order.create(
            ORDER_ID, "John", "invalid-email", NOW, OrderStatus.PENDING, "USD",
            DELIVERY, INVOICE, LINES));
  }

  @Test
  void blankCustomerEmailThrowsException() {
    assertThrows(
        InvalidOrderDataException.class,
        () -> Order.create(
            ORDER_ID, "John", "   ", NOW, OrderStatus.PENDING, "USD",
            DELIVERY, INVOICE, LINES));
  }

  @Test
  void overlongCustomerEmailThrowsException() {
    String longEmail = "a".repeat(245) + "@example.com";
    assertThrows(
        InvalidOrderDataException.class,
        () -> Order.create(
            ORDER_ID, "John", longEmail, NOW, OrderStatus.PENDING, "USD",
            DELIVERY, INVOICE, LINES));
  }

  @Test
  void nullDateThrowsException() {
    assertThrows(
        InvalidOrderDataException.class,
        () -> Order.create(
            ORDER_ID, "John", "john@example.com", null, OrderStatus.PENDING, "USD",
            DELIVERY, INVOICE, LINES));
  }

  @Test
  void nullStatusThrowsException() {
    assertThrows(
        InvalidOrderDataException.class,
        () -> Order.create(
            ORDER_ID, "John", "john@example.com", NOW, null, "USD",
            DELIVERY, INVOICE, LINES));
  }
}
