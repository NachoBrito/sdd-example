package es.nachobrito.sdd.example.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderTest {

  private static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");

  private static DeliveryAddress sampleDeliveryAddress() {
    return new DeliveryAddress(
        "Alice Smith", "+1-555-123-4567", "123 Main St", "Apt 4B",
        "Springfield", "IL", "62701", "USA");
  }

  private static InvoiceAddress sampleInvoiceAddress() {
    return new InvoiceAddress(
        "Acme Corp", "US123456789", "+1-555-987-6543", "456 Market St", null,
        "Chicago", "IL", "60601", "USA");
  }

  private static OrderLine sampleOrderLine(int quantity, int unitPriceInCents) {
    return new OrderLine(PRODUCT_ID, quantity, unitPriceInCents);
  }

  @Test
  void createOrderCalculatesTotalAmount() {
    OrderLine line1 = sampleOrderLine(2, 5_000);
    OrderLine line2 = sampleOrderLine(3, 1_000);
    Order order = Order.create(
        ORDER_ID, "John Doe", "john.doe@example.com", NOW, OrderStatus.PENDING, "USD",
        sampleDeliveryAddress(), sampleInvoiceAddress(), List.of(line1, line2));
    assertEquals(13_000L, order.totalAmountInCents());
  }

  @Test
  void orderLineCalculatesLineTotal() {
    OrderLine line = new OrderLine(PRODUCT_ID, 4, 2_500);
    assertEquals(10_000L, line.lineTotalInCents());
  }

  @Test
  void withUpdatedAttributesModifiesCustomerName() {
    Order original = Order.create(
        ORDER_ID, "John Doe", "john.doe@example.com", NOW, OrderStatus.PENDING, "USD",
        sampleDeliveryAddress(), sampleInvoiceAddress(), List.of(sampleOrderLine(1, 1_000)));
    Instant later = NOW.plusSeconds(3_600);
    Order updated = original.withUpdatedAttributes(
        "Jane Doe", "jane.doe@example.com", NOW, OrderStatus.PAID, "USD",
        sampleDeliveryAddress(), sampleInvoiceAddress(),
        List.of(sampleOrderLine(2, 1_000)), later);
    assertEquals("Jane Doe", updated.customerName());
  }

  @Test
  void withUpdatedAttributesRecalculatesTotalAmount() {
    Order original = Order.create(
        ORDER_ID, "John Doe", "john.doe@example.com", NOW, OrderStatus.PENDING, "USD",
        sampleDeliveryAddress(), sampleInvoiceAddress(), List.of(sampleOrderLine(1, 1_000)));
    Instant later = NOW.plusSeconds(3_600);
    Order updated = original.withUpdatedAttributes(
        "Jane Doe", "jane.doe@example.com", NOW, OrderStatus.PAID, "USD",
        sampleDeliveryAddress(), sampleInvoiceAddress(),
        List.of(sampleOrderLine(2, 1_000)), later);
    assertEquals(2_000L, updated.totalAmountInCents());
  }

  @Test
  void withUpdatedAttributesPreservesCreatedAt() {
    Order original = Order.create(
        ORDER_ID, "John Doe", "john.doe@example.com", NOW, OrderStatus.PENDING, "USD",
        sampleDeliveryAddress(), sampleInvoiceAddress(), List.of(sampleOrderLine(1, 1_000)));
    Instant later = NOW.plusSeconds(3_600);
    Order updated = original.withUpdatedAttributes(
        "Jane Doe", "jane.doe@example.com", NOW, OrderStatus.PAID, "USD",
        sampleDeliveryAddress(), sampleInvoiceAddress(),
        List.of(sampleOrderLine(2, 1_000)), later);
    assertEquals(NOW, updated.createdAt());
  }

  @Test
  void withUpdatedAttributesRefreshesUpdatedAt() {
    Order original = Order.create(
        ORDER_ID, "John Doe", "john.doe@example.com", NOW, OrderStatus.PENDING, "USD",
        sampleDeliveryAddress(), sampleInvoiceAddress(), List.of(sampleOrderLine(1, 1_000)));
    Instant later = NOW.plusSeconds(3_600);
    Order updated = original.withUpdatedAttributes(
        "Jane Doe", "jane.doe@example.com", NOW, OrderStatus.PAID, "USD",
        sampleDeliveryAddress(), sampleInvoiceAddress(),
        List.of(sampleOrderLine(2, 1_000)), later);
    assertEquals(later, updated.updatedAt());
  }
}
