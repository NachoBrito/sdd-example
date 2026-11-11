package es.nachobrito.sdd.example.adapter.messaging;

import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderEventType;
import es.nachobrito.sdd.example.domain.model.OrderLifecycleEvent;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Message envelope DTO for order lifecycle events published to RabbitMQ.
 *
 * @param eventId the unique event identifier
 * @param eventType the event type name
 * @param timestamp the event timestamp in UTC
 * @param order the order details payload
 */
@Serdeable
public record OrderEventDto(
    UUID eventId,
    String eventType,
    Instant timestamp,
    OrderPayloadDto order) {

  /**
   * Validates required event envelope fields.
   */
  public OrderEventDto {
    Objects.requireNonNull(eventId, "EventId must not be null");
    Objects.requireNonNull(eventType, "EventType must not be null");
    Objects.requireNonNull(timestamp, "Timestamp must not be null");
    Objects.requireNonNull(order, "Order must not be null");
  }

  /**
   * Creates an order event DTO from a domain order and event type.
   *
   * @param eventType the domain event type
   * @param order the domain order
   * @return the order event DTO
   */
  public static OrderEventDto fromDomain(OrderEventType eventType, Order order) {
    Objects.requireNonNull(eventType, "EventType must not be null");
    Objects.requireNonNull(order, "Order must not be null");
    return new OrderEventDto(
        UUID.randomUUID(),
        eventType.name(),
        Instant.now(),
        OrderPayloadDto.fromDomain(order));
  }

  /**
   * Converts this event DTO to a domain order lifecycle event.
   *
   * @return the domain order lifecycle event
   */
  public OrderLifecycleEvent toDomain() {
    return new OrderLifecycleEvent(
        eventId,
        OrderEventType.valueOf(eventType),
        timestamp,
        order.toDomain(timestamp));
  }
}

