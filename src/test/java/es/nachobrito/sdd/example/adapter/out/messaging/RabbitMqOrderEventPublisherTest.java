package es.nachobrito.sdd.example.adapter.out.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import es.nachobrito.sdd.example.adapter.messaging.OrderEventDto;
import es.nachobrito.sdd.example.domain.model.DeliveryAddress;
import es.nachobrito.sdd.example.domain.model.InvoiceAddress;
import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderEventType;
import es.nachobrito.sdd.example.domain.model.OrderLine;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RabbitMqOrderEventPublisherTest {

  private OrderEventClient client;
  private RabbitMqOrderEventPublisher publisher;
  private Order order;

  @BeforeEach
  void setUp() {
    client = mock(OrderEventClient.class);
    publisher = new RabbitMqOrderEventPublisher(client);

    DeliveryAddress delivery = new DeliveryAddress(
        "Bob", "+123456", "Street", null, "City", "State", "12345", "Country");
    InvoiceAddress invoice = new InvoiceAddress(
        "Bob", "VAT123", "+123456", "Street", null, "City", "State", "12345", "Country");
    OrderLine line = new OrderLine(UUID.randomUUID(), 1, 1500, 1500);

    order = new Order(
        UUID.randomUUID(),
        UUID.randomUUID(),
        "Bob",
        "bob@example.com",
        Instant.now(),
        OrderStatus.PENDING,
        1500,
        "EUR",
        delivery,
        invoice,
        List.of(line),
        Instant.now(),
        Instant.now());
  }

  @Test
  void publishesOrderCreatedEvent() {
    publisher.publishOrderCreated(order);

    ArgumentCaptor<OrderEventDto> captor = ArgumentCaptor.forClass(OrderEventDto.class);
    verify(client).sendOrderCreated(captor.capture());

    OrderEventDto event = captor.getValue();
    assertEquals(OrderEventType.CREATED.name(), event.eventType());
    assertEquals(order.id(), event.order().id());
    assertNotNull(event.eventId());
    assertNotNull(event.timestamp());
  }

  @Test
  void publishesOrderUpdatedEvent() {
    publisher.publishOrderUpdated(order);

    ArgumentCaptor<OrderEventDto> captor = ArgumentCaptor.forClass(OrderEventDto.class);
    verify(client).sendOrderUpdated(captor.capture());

    OrderEventDto event = captor.getValue();
    assertEquals(OrderEventType.UPDATED.name(), event.eventType());
    assertEquals(order.id(), event.order().id());
  }

  @Test
  void publishesOrderCancelledEvent() {
    publisher.publishOrderCancelled(order);

    ArgumentCaptor<OrderEventDto> captor = ArgumentCaptor.forClass(OrderEventDto.class);
    verify(client).sendOrderCancelled(captor.capture());

    OrderEventDto event = captor.getValue();
    assertEquals(OrderEventType.CANCELLED.name(), event.eventType());
    assertEquals(order.id(), event.order().id());
  }
}
