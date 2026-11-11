package es.nachobrito.sdd.example.application.port.out;

import es.nachobrito.sdd.example.domain.model.NotificationChannel;
import es.nachobrito.sdd.example.domain.model.OrderLifecycleEvent;

/**
 * Outbound port representing a notification transport mechanism.
 */
public interface NotificationTransportPort {

  /**
   * Returns the communication channel supported by this transport.
   *
   * @return the notification channel
   */
  NotificationChannel channel();

  /**
   * Sends a notification for an order lifecycle event.
   *
   * @param event the order lifecycle event
   */
  void sendNotification(OrderLifecycleEvent event);
}

