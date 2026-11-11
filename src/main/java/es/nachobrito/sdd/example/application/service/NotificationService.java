package es.nachobrito.sdd.example.application.service;

import es.nachobrito.sdd.example.application.port.in.ProcessNotificationUseCase;
import es.nachobrito.sdd.example.application.port.out.NotificationTransportPort;
import es.nachobrito.sdd.example.domain.model.OrderLifecycleEvent;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service that processes order lifecycle events and dispatches notifications.
 */
@Singleton
public class NotificationService implements ProcessNotificationUseCase {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

  private final List<NotificationTransportPort> transports;

  /**
   * Constructs the notification service with available transport ports.
   *
   * @param transports the list of registered notification transports
   */
  public NotificationService(List<NotificationTransportPort> transports) {
    this.transports = transports != null ? List.copyOf(transports) : List.of();
  }

  @Override
  public void processOrderEvent(OrderLifecycleEvent event) {
    Objects.requireNonNull(event, "Event must not be null");

    if (LOG.isInfoEnabled()) {
      LOG.info(
          "processing_order_event: event_id={}, event_type={}, order_id={}, transport_count={}",
          event.eventId(),
          event.eventType(),
          event.order().id(),
          transports.size());
    }

    for (NotificationTransportPort transport : transports) {
      try {
        transport.sendNotification(event);
      } catch (IllegalArgumentException | IllegalStateException e) {
        if (LOG.isErrorEnabled()) {
          LOG.error(
              "transport_execution_failed: channel={}, event_id={}, order_id={}, error={}",
              transport.channel(),
              event.eventId(),
              event.order().id(),
              e.getMessage(),
              e);
        }
      }
    }
  }
}
