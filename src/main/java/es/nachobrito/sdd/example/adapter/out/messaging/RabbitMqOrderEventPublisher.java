package es.nachobrito.sdd.example.adapter.out.messaging;

import es.nachobrito.sdd.example.adapter.messaging.OrderEventDto;
import es.nachobrito.sdd.example.application.port.out.OrderEventPublisherPort;
import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderEventType;
import jakarta.inject.Singleton;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Outbound adapter that publishes order lifecycle events to RabbitMQ.
 */
@Singleton
public class RabbitMqOrderEventPublisher implements OrderEventPublisherPort {

  private static final Logger LOG = LoggerFactory.getLogger(RabbitMqOrderEventPublisher.class);

  private final OrderEventClient client;

  /**
   * Constructs the publisher with the declarative messaging client.
   *
   * @param client the RabbitMQ client interface
   */
  public RabbitMqOrderEventPublisher(OrderEventClient client) {
    this.client = client;
  }

  @Override
  public void publishOrderCreated(Order order) {
    Objects.requireNonNull(order, "Order must not be null");
    OrderEventDto event = OrderEventDto.fromDomain(OrderEventType.CREATED, order);
    if (LOG.isInfoEnabled()) {
      LOG.info("publishing_order_created: event_id={}, order_id={}", event.eventId(), order.id());
    }
    client.sendOrderCreated(event);
  }

  @Override
  public void publishOrderUpdated(Order order) {
    Objects.requireNonNull(order, "Order must not be null");
    OrderEventDto event = OrderEventDto.fromDomain(OrderEventType.UPDATED, order);
    if (LOG.isInfoEnabled()) {
      LOG.info("publishing_order_updated: event_id={}, order_id={}", event.eventId(), order.id());
    }
    client.sendOrderUpdated(event);
  }

  @Override
  public void publishOrderCancelled(Order order) {
    Objects.requireNonNull(order, "Order must not be null");
    OrderEventDto event = OrderEventDto.fromDomain(OrderEventType.CANCELLED, order);
    if (LOG.isInfoEnabled()) {
      LOG.info("publishing_order_cancelled: event_id={}, order_id={}", event.eventId(), order.id());
    }
    client.sendOrderCancelled(event);
  }
}
