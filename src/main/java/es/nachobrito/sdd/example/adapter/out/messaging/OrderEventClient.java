package es.nachobrito.sdd.example.adapter.out.messaging;

import es.nachobrito.sdd.example.adapter.messaging.OrderEventDto;
import es.nachobrito.sdd.example.adapter.messaging.RabbitMqConstants;
import io.micronaut.rabbitmq.annotation.Binding;
import io.micronaut.rabbitmq.annotation.RabbitClient;
import java.util.concurrent.CompletableFuture;

/**
 * Declarative RabbitMQ client publishing order lifecycle events to exchange sdd.orders.
 */
@RabbitClient(RabbitMqConstants.EXCHANGE_ORDERS)
public interface OrderEventClient {

  /**
   * Publishes an order created event asynchronously.
   *
   * @param event the lifecycle event DTO
   * @return future completed when the message is dispatched
   */
  @Binding(RabbitMqConstants.ROUTING_KEY_ORDER_CREATED)
  CompletableFuture<Void> sendOrderCreated(OrderEventDto event);

  /**
   * Publishes an order updated event asynchronously.
   *
   * @param event the lifecycle event DTO
   * @return future completed when the message is dispatched
   */
  @Binding(RabbitMqConstants.ROUTING_KEY_ORDER_UPDATED)
  CompletableFuture<Void> sendOrderUpdated(OrderEventDto event);

  /**
   * Publishes an order cancelled event asynchronously.
   *
   * @param event the lifecycle event DTO
   * @return future completed when the message is dispatched
   */
  @Binding(RabbitMqConstants.ROUTING_KEY_ORDER_CANCELLED)
  CompletableFuture<Void> sendOrderCancelled(OrderEventDto event);
}
