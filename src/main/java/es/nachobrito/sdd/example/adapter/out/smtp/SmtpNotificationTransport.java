package es.nachobrito.sdd.example.adapter.out.smtp;

import es.nachobrito.sdd.example.application.port.out.NotificationTransportPort;
import es.nachobrito.sdd.example.domain.model.NotificationChannel;
import es.nachobrito.sdd.example.domain.model.NotificationMessage;
import es.nachobrito.sdd.example.domain.model.OrderLifecycleEvent;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Outbound transport delivering email notifications through SMTP.
 */
@Singleton
public class SmtpNotificationTransport implements NotificationTransportPort {

  private static final Logger LOG = LoggerFactory.getLogger(SmtpNotificationTransport.class);

  private final SmtpClient smtpClient;
  private final SmtpOrderHtmlTemplate template;
  private final String host;
  private final int port;
  private final String from;
  private final int timeoutMs;

  /**
   * Constructs the SMTP transport adapter with configuration properties.
   *
   * @param smtpClient client for network SMTP transmission
   * @param template template renderer for HTML emails
   * @param host SMTP host
   * @param port SMTP port
   * @param from default sender email address
   * @param timeoutMs connection and read timeout
   */
  public SmtpNotificationTransport(
      SmtpClient smtpClient,
      SmtpOrderHtmlTemplate template,
      @Value("${notification.smtp.host:localhost}") String host,
      @Value("${notification.smtp.port:2525}") int port,
      @Value("${notification.smtp.from:no-reply@example.com}") String from,
      @Value("${notification.smtp.timeout-ms:5000}") int timeoutMs) {
    this.smtpClient = smtpClient;
    this.template = template;
    this.host = host;
    this.port = port;
    this.from = from;
    this.timeoutMs = timeoutMs;
  }

  @Override
  public NotificationChannel channel() {
    return NotificationChannel.SMTP;
  }

  @Override
  public void sendNotification(OrderLifecycleEvent event) {
    Objects.requireNonNull(event, "Event must not be null");

    try {
      String recipient = event.order().customerEmail();
      String subject = template.renderSubject(event);
      String body = template.renderBody(event);

      NotificationMessage message = new NotificationMessage(
          recipient,
          subject,
          body,
          NotificationChannel.SMTP);

      smtpClient.sendEmail(
          host,
          port,
          timeoutMs,
          from,
          message.recipient(),
          message.subject(),
          message.body());

      if (LOG.isInfoEnabled()) {
        LOG.info(
            "smtp_notification_sent: event_id={}, order_id={}, recipient={}",
            event.eventId(),
            event.order().id(),
            recipient);
      }
    } catch (IllegalArgumentException e) {
      if (LOG.isErrorEnabled()) {
        LOG.error(
            "smtp_notification_invalid: event_id={}, order_id={}, error={}",
            event.eventId(),
            event.order().id(),
            e.getMessage());
      }
    } catch (IOException e) {
      if (LOG.isErrorEnabled()) {
        LOG.error(
            "smtp_notification_failed: event_id={}, order_id={}, error={}",
            event.eventId(),
            event.order().id(),
            e.getMessage());
      }
    }
  }
}
