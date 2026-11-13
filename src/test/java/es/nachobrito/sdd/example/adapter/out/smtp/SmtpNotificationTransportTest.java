package es.nachobrito.sdd.example.adapter.out.smtp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.domain.model.DeliveryAddress;
import es.nachobrito.sdd.example.domain.model.InvoiceAddress;
import es.nachobrito.sdd.example.domain.model.NotificationChannel;
import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderEventType;
import es.nachobrito.sdd.example.domain.model.OrderLifecycleEvent;
import es.nachobrito.sdd.example.domain.model.OrderLine;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SmtpNotificationTransportTest {

  private SmtpClient smtpClient;
  private SmtpOrderHtmlTemplate template;
  private SmtpNotificationTransport transport;
  private OrderLifecycleEvent event;

  @BeforeEach
  void setUp() {
    smtpClient = mock(SmtpClient.class);
    template = new SmtpOrderHtmlTemplate();
    transport = new SmtpNotificationTransport(
        smtpClient,
        template,
        "mail.example.com",
        2525,
        "store@example.com",
        3000);

    DeliveryAddress delivery = new DeliveryAddress(
        "Diana", "+123", "Street", null, "City", "State", "123", "Country");
    InvoiceAddress invoice = new InvoiceAddress(
        "Diana", "VAT123", "+123", "Street", null, "City", "State", "123", "Country");
    OrderLine line = new OrderLine(UUID.randomUUID(), 1, 2000, 2000);
    Order order = new Order(
        UUID.randomUUID(),
        UUID.randomUUID(),
        "Diana",
        "diana@example.com",
        Instant.now(),
        OrderStatus.PENDING,
        2000,
        "EUR",
        delivery,
        invoice,
        List.of(line),
        Instant.now(),
        Instant.now());

    event = new OrderLifecycleEvent(
        UUID.randomUUID(), OrderEventType.CREATED, Instant.now(), order);
  }

  @Test
  void channelReturnsSmtp() {
    assertEquals(NotificationChannel.SMTP, transport.channel());
  }

  @Test
  void sendsEmailSuccessfully() throws IOException {
    transport.sendNotification(event);

    verify(smtpClient).sendEmail(
        eq("mail.example.com"),
        eq(2525),
        eq(3000),
        eq("store@example.com"),
        eq("diana@example.com"),
        eq(template.renderSubject(event)),
        eq(template.renderBody(event)));
  }

  @Test
  void catchesAndLogsIoExceptionWithoutThrowing() throws IOException {
    doThrow(new IOException("Connection refused"))
        .when(smtpClient)
        .sendEmail(
            anyString(), anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());

    assertDoesNotThrow(() -> transport.sendNotification(event));
  }

  @Test
  void catchesAndLogsInvalidEmailWithoutThrowing() {
    Order mockedOrder = mock(Order.class);
    when(mockedOrder.customerEmail()).thenReturn("   ");
    when(mockedOrder.id()).thenReturn(UUID.randomUUID());

    SmtpOrderHtmlTemplate mockedTemplate = mock(SmtpOrderHtmlTemplate.class);
    when(mockedTemplate.renderSubject(any())).thenReturn("Subject");
    when(mockedTemplate.renderBody(any())).thenReturn("Body");

    SmtpNotificationTransport transportWithMockTemplate = new SmtpNotificationTransport(
        smtpClient,
        mockedTemplate,
        "mail.example.com",
        2525,
        "store@example.com",
        3000);

    OrderLifecycleEvent badEvent = new OrderLifecycleEvent(
        UUID.randomUUID(), OrderEventType.CREATED, Instant.now(), mockedOrder);

    assertDoesNotThrow(() -> transportWithMockTemplate.sendNotification(badEvent));
  }
}
