package es.nachobrito.sdd.example.adapter.in.messaging;

import es.nachobrito.sdd.example.adapter.messaging.OrderEventDto;
import es.nachobrito.sdd.example.adapter.messaging.RabbitMqConstants;
import es.nachobrito.sdd.example.application.port.in.ProcessNotificationUseCase;
import es.nachobrito.sdd.example.domain.model.OrderLifecycleEvent;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inbound messaging adapter that receives order events from RabbitMQ and triggers notifications.
 */
@RabbitListener
public class OrderNotificationConsumer {

  private static final Logger LOG = LoggerFactory.getLogger(OrderNotificationConsumer.class);

  private final ProcessNotificationUseCase processNotificationUseCase;

  /**
   * Constructs the message consumer with the notification processing use case.
   *
   * @param processNotificationUseCase the use case port
   */
  public OrderNotificationConsumer(ProcessNotificationUseCase processNotificationUseCase) {
    this.processNotificationUseCase = processNotificationUseCase;
  }

  /**
   * Consumes an order lifecycle event from the notification queue.
   *
   * @param eventDto the received order lifecycle event DTO
   */
  @Queue(RabbitMqConstants.QUEUE_NOTIFICATIONS)
  public void receive(OrderEventDto eventDto) {
    Objects.requireNonNull(eventDto, "EventDto must not be null");
    if (LOG.isInfoEnabled()) {
      LOG.info(
          "received_order_event: event_id={}, event_type={}, order_id={}",
          eventDto.eventId(),
          eventDto.eventType(),
          eventDto.order().id());
    }
    OrderLifecycleEvent domainEvent = eventDto.toDomain();
    processNotificationUseCase.processOrderEvent(domainEvent);
  }
}
