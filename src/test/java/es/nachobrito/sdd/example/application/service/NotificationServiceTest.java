package es.nachobrito.sdd.example.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import es.nachobrito.sdd.example.application.port.out.NotificationTransportPort;
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

class NotificationServiceTest {

  private NotificationTransportPort transport1;
  private NotificationTransportPort transport2;
  private NotificationService service;
  private OrderLifecycleEvent event;

  @BeforeEach
  void setUp() {
    transport1 = mock(NotificationTransportPort.class);
    transport2 = mock(NotificationTransportPort.class);
    service = new NotificationService(List.of(transport1, transport2));

    DeliveryAddress delivery = new DeliveryAddress(
        "Alice", "+12345", "Street 1", null, "City", "State", "12345", "Country");
    InvoiceAddress invoice = new InvoiceAddress(
        "Alice", "VAT1", "+12345", "Street 1", null, "City", "State", "12345", "Country");
    OrderLine line = new OrderLine(UUID.randomUUID(), 1, 1000, 1000);
    Order order = new Order(
        UUID.randomUUID(),
        UUID.randomUUID(),
        "Alice",
        "alice@example.com",
        Instant.now(),
        OrderStatus.PENDING,
        1000,
        "EUR",
        delivery,
        invoice,
        List.of(line),
        Instant.now(),
        Instant.now());

    event = new OrderLifecycleEvent(
        UUID.randomUUID(), OrderEventType.CREATED, Instant.now(), order);
  }

  @Test
  void throwsNullPointerExceptionWhenEventIsNull() {
    assertThrows(NullPointerException.class, () -> service.processOrderEvent(null));
  }

  @Test
  void handlesNullTransportsListGracefully() {
    NotificationService emptyService = new NotificationService(null);
    assertDoesNotThrow(() -> emptyService.processOrderEvent(event));
  }

  @Test
  void delegatesEventToAllRegisteredTransports() {
    service.processOrderEvent(event);

    verify(transport1).sendNotification(event);
    verify(transport2).sendNotification(event);
  }

  @Test
  void continuesDispatchingWhenOneTransportFails() {
    doThrow(new IllegalStateException("Transport failure"))
        .when(transport1)
        .sendNotification(event);

    service.processOrderEvent(event);

    verify(transport1).sendNotification(event);
    verify(transport2).sendNotification(event);
  }
}
