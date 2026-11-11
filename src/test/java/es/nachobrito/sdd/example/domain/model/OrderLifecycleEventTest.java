package es.nachobrito.sdd.example.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderLifecycleEventTest {

  private Order order;
  private UUID orderId;

  @BeforeEach
  void setUp() {
    orderId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();

    DeliveryAddress delivery = new DeliveryAddress(
        "Eve", "+123", "Street", "Suite 1", "City", "State", "123", "Country");
    InvoiceAddress invoice = new InvoiceAddress(
        "Eve", "VAT123", "+123", "Street", null, "City", "State", "123", "Country");
    OrderLine line = new OrderLine(productId, 2, 1000, 2000);

    order = new Order(
        orderId,
        "Eve",
        "eve@example.com",
        Instant.now(),
        OrderStatus.PENDING,
        2000,
        "EUR",
        delivery,
        invoice,
        List.of(line),
        Instant.now(),
        Instant.now());
  }

  @Test
  void createsEventFromDomainOrder() {
    OrderLifecycleEvent event = OrderLifecycleEvent.from(OrderEventType.CREATED, order);

    assertNotNull(event.eventId());
    assertEquals(OrderEventType.CREATED, event.eventType());
    assertNotNull(event.timestamp());
    assertEquals(orderId, event.order().id());
    assertEquals(2000, event.order().totalAmountInCents());
    assertEquals("Suite 1", event.order().deliveryAddress().complement());
    assertEquals(1, event.order().orderLines().size());
  }

  @Test
  void createsEventWithExplicitTimestampAndId() {
    UUID eventId = UUID.randomUUID();
    Instant now = Instant.now();
    OrderLifecycleEvent event =
        OrderLifecycleEvent.from(eventId, OrderEventType.CANCELLED, now, order);

    assertEquals(eventId, event.eventId());
    assertEquals(OrderEventType.CANCELLED, event.eventType());
    assertEquals(now, event.timestamp());
    assertEquals(order, event.order());
  }

  @Test
  void throwsExceptionWhenRequiredFieldsAreNull() {
    UUID eventId = UUID.randomUUID();
    Instant now = Instant.now();

    assertThrows(NullPointerException.class, () ->
        new OrderLifecycleEvent(null, OrderEventType.CREATED, now, order));

    assertThrows(NullPointerException.class, () ->
        new OrderLifecycleEvent(eventId, null, now, order));

    assertThrows(NullPointerException.class, () ->
        new OrderLifecycleEvent(eventId, OrderEventType.CREATED, null, order));

    assertThrows(NullPointerException.class, () ->
        new OrderLifecycleEvent(eventId, OrderEventType.CREATED, now, null));
  }
}
