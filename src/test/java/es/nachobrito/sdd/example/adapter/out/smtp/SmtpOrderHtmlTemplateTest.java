package es.nachobrito.sdd.example.adapter.out.smtp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.nachobrito.sdd.example.domain.model.DeliveryAddress;
import es.nachobrito.sdd.example.domain.model.InvoiceAddress;
import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderEventType;
import es.nachobrito.sdd.example.domain.model.OrderLifecycleEvent;
import es.nachobrito.sdd.example.domain.model.OrderLine;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SmtpOrderHtmlTemplateTest {

  private SmtpOrderHtmlTemplate template;
  private OrderLifecycleEvent event;
  private UUID orderId;
  private UUID productId;
  private Order order;

  @BeforeEach
  void setUp() {
    template = new SmtpOrderHtmlTemplate();
    orderId = UUID.randomUUID();
    productId = UUID.randomUUID();

    DeliveryAddress delivery = new DeliveryAddress(
        "John & Jane Doe",
        "+123456789",
        "123 Main <Street>",
        "Apt \"4B\"",
        "Springfield",
        "IL",
        "62701",
        "USA");

    InvoiceAddress invoice = new InvoiceAddress(
        "John & Jane Doe",
        "VAT999",
        "+123456789",
        "123 Main <Street>",
        null,
        "Springfield",
        "IL",
        "62701",
        "USA");

    OrderLine line = new OrderLine(productId, 2, 1500, 3000);

    order = new Order(
        orderId,
        "John & Jane Doe",
        "john@example.com",
        Instant.parse("2026-11-11T10:00:00Z"),
        OrderStatus.PENDING,
        3000,
        "EUR",
        delivery,
        invoice,
        List.of(line),
        Instant.now(),
        Instant.now());

    event = new OrderLifecycleEvent(
        UUID.randomUUID(),
        OrderEventType.CREATED,
        Instant.now(),
        order);
  }

  @Test
  void rendersSubjectForCreatedEvent() {
    String subject = template.renderSubject(event);
    assertTrue(subject.contains("Order Confirmation"));
    assertTrue(subject.contains(orderId.toString()));
  }

  @Test
  void rendersSubjectForUpdatedEvent() {
    OrderLifecycleEvent updatedEvent = new OrderLifecycleEvent(
        event.eventId(), OrderEventType.UPDATED, event.timestamp(), event.order());
    String subject = template.renderSubject(updatedEvent);
    assertTrue(subject.contains("Order Update"));
    assertTrue(subject.contains(orderId.toString()));
  }

  @Test
  void rendersSubjectForCancelledEvent() {
    OrderLifecycleEvent cancelledEvent = new OrderLifecycleEvent(
        event.eventId(), OrderEventType.CANCELLED, event.timestamp(), event.order());
    String subject = template.renderSubject(cancelledEvent);
    assertTrue(subject.contains("Order Cancellation"));
    assertTrue(subject.contains(orderId.toString()));
  }

  @Test
  void rendersBodyWithEscapedValuesAndOrderDetails() {
    String html = template.renderBody(event);

    assertTrue(html.contains("<!DOCTYPE html>"));
    assertTrue(html.contains("John &amp; Jane Doe"));
    assertFalse(html.contains("<Street>"));
    assertTrue(html.contains("&lt;Street&gt;"));
    assertTrue(html.contains("&quot;4B&quot;"));
    assertTrue(html.contains(orderId.toString()));
    assertTrue(html.contains(productId.toString()));
    assertTrue(html.contains("EUR"));
    assertTrue(html.contains("30.00 EUR"));
    assertTrue(html.contains("15.00 EUR"));
    assertTrue(html.contains("Springfield"));
  }

  @Test
  void rendersBodyForUpdatedAndCancelledEvents() {
    OrderLifecycleEvent updatedEvent = new OrderLifecycleEvent(
        event.eventId(), OrderEventType.UPDATED, event.timestamp(), event.order());
    String updatedHtml = template.renderBody(updatedEvent);
    assertTrue(updatedHtml.contains("Order Update Notification"));

    OrderLifecycleEvent cancelledEvent = new OrderLifecycleEvent(
        event.eventId(), OrderEventType.CANCELLED, event.timestamp(), event.order());
    String cancelledHtml = template.renderBody(cancelledEvent);
    assertTrue(cancelledHtml.contains("Order Cancellation Notification"));
  }

  @Test
  void rendersDeliveryAddressWithoutComplementWhenComplementIsNull() {
    DeliveryAddress noComplementAddress = new DeliveryAddress(
        "Bob", "+123", "Street", null, "City", "State", "123", "Country");
    Order orderWithoutComplement = new Order(
        UUID.randomUUID(),
        "Bob",
        "bob@example.com",
        Instant.now(),
        OrderStatus.PENDING,
        3000,
        "EUR",
        noComplementAddress,
        order.invoiceAddress(),
        order.orderLines(),
        Instant.now(),
        Instant.now());

    OrderLifecycleEvent evt =
        OrderLifecycleEvent.from(OrderEventType.CREATED, orderWithoutComplement);
    String html = template.renderBody(evt);
    assertTrue(html.contains("Bob"));
    assertTrue(html.contains("Street"));
  }
}
