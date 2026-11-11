package es.nachobrito.sdd.example.application.port.in;

import es.nachobrito.sdd.example.domain.model.OrderLifecycleEvent;

/**
 * Inbound port for processing order lifecycle events into notifications.
 */
@FunctionalInterface
public interface ProcessNotificationUseCase {

  /**
   * Processes an order lifecycle event and dispatches notifications across transports.
   *
   * @param event the order lifecycle event
   */
  void processOrderEvent(OrderLifecycleEvent event);
}
