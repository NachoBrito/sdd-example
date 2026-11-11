package es.nachobrito.sdd.example.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain event representing a change in the life cycle of an order.
 *
 * @param eventId the unique event identifier
 * @param eventType the type of the order event
 * @param timestamp the event timestamp in UTC
 * @param order the domain order
 */
public record OrderLifecycleEvent(
    UUID eventId,
    OrderEventType eventType,
    Instant timestamp,
    Order order) {

  /**
   * Validates the event envelope attributes.
   */
  public OrderLifecycleEvent {
    Objects.requireNonNull(eventId, "EventId must not be null");
    Objects.requireNonNull(eventType, "EventType must not be null");
    Objects.requireNonNull(timestamp, "Timestamp must not be null");
    Objects.requireNonNull(order, "Order must not be null");
  }

  /**
   * Creates an event from a domain order and an event type.
   *
   * @param eventType the event type
   * @param order the domain order
   * @return the order life cycle event
   */
  public static OrderLifecycleEvent from(OrderEventType eventType, Order order) {
    return from(UUID.randomUUID(), eventType, Instant.now(), order);
  }

  /**
   * Creates an event with an explicit identifier and timestamp.
   *
   * @param eventId the event identifier
   * @param eventType the event type
   * @param timestamp the event timestamp
   * @param order the domain order
   * @return the order life cycle event
   */
  public static OrderLifecycleEvent from(
      UUID eventId,
      OrderEventType eventType,
      Instant timestamp,
      Order order) {
    return new OrderLifecycleEvent(eventId, eventType, timestamp, order);
  }
}
