package es.nachobrito.sdd.example.adapter.in.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import es.nachobrito.sdd.example.adapter.messaging.AddressPayloadDto;
import es.nachobrito.sdd.example.adapter.messaging.OrderEventDto;
import es.nachobrito.sdd.example.adapter.messaging.OrderLinePayloadDto;
import es.nachobrito.sdd.example.adapter.messaging.OrderPayloadDto;
import es.nachobrito.sdd.example.application.port.in.ProcessNotificationUseCase;
import es.nachobrito.sdd.example.domain.model.OrderEventType;
import es.nachobrito.sdd.example.domain.model.OrderLifecycleEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class OrderNotificationConsumerTest {

  private ProcessNotificationUseCase useCase;
  private OrderNotificationConsumer consumer;
  private OrderEventDto eventDto;
  private UUID orderId;

  @BeforeEach
  void setUp() {
    useCase = mock(ProcessNotificationUseCase.class);
    consumer = new OrderNotificationConsumer(useCase);
    orderId = UUID.randomUUID();

    AddressPayloadDto address = new AddressPayloadDto(
        "Charlie", "+123", "Street", null, "City", "State", "123", "Country");
    OrderLinePayloadDto line = new OrderLinePayloadDto(UUID.randomUUID(), 1, 500, 500);
    OrderPayloadDto order = new OrderPayloadDto(
        orderId,
        "Charlie",
        "charlie@example.com",
        Instant.now(),
        "PENDING",
        500,
        "EUR",
        address,
        address,
        List.of(line));

    eventDto = new OrderEventDto(UUID.randomUUID(), "CREATED", Instant.now(), order);
  }

  @Test
  void receiveDelegatesEventToProcessNotificationUseCase() {
    consumer.receive(eventDto);

    ArgumentCaptor<OrderLifecycleEvent> captor = ArgumentCaptor.forClass(OrderLifecycleEvent.class);
    verify(useCase).processOrderEvent(captor.capture());

    OrderLifecycleEvent captured = captor.getValue();
    assertEquals(eventDto.eventId(), captured.eventId());
    assertEquals(OrderEventType.CREATED, captured.eventType());
    assertEquals(orderId, captured.order().id());
  }
}
