package es.nachobrito.sdd.example.adapter.messaging;

/**
 * Declares the RabbitMQ topology constants for order events.
 */
public final class RabbitMqConstants {

  /**
   * The topic exchange name for order events.
   */
  public static final String EXCHANGE_ORDERS = "sdd.orders";

  /**
   * The queue name for asynchronous notifications.
   */
  public static final String QUEUE_NOTIFICATIONS = "sdd.orders.notifications";

  /**
   * The routing key for order created events.
   */
  public static final String ROUTING_KEY_ORDER_CREATED = "order.created";

  /**
   * The routing key for order updated events.
   */
  public static final String ROUTING_KEY_ORDER_UPDATED = "order.updated";

  /**
   * The routing key for order cancelled events.
   */
  public static final String ROUTING_KEY_ORDER_CANCELLED = "order.cancelled";

  /**
   * The wildcard binding key for all order events.
   */
  public static final String BINDING_KEY_ORDER_ALL = "order.*";

  private RabbitMqConstants() {}
}

