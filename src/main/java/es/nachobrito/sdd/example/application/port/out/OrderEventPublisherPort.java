package es.nachobrito.sdd.example.application.port.out;

import es.nachobrito.sdd.example.domain.model.Order;

/**
 * Outbound port for publishing order lifecycle events to the messaging broker.
 */
public interface OrderEventPublisherPort {

  /**
   * Publishes an event indicating that a new order was created.
   *
   * @param order the created order
   */
  void publishOrderCreated(Order order);

  /**
   * Publishes an event indicating that an existing order was updated.
   *
   * @param order the updated order
   */
  void publishOrderUpdated(Order order);

  /**
   * Publishes an event indicating that an order was cancelled or deleted.
   *
   * @param order the cancelled order
   */
  void publishOrderCancelled(Order order);
}

