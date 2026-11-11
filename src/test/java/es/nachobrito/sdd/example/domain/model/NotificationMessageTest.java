package es.nachobrito.sdd.example.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NotificationMessageTest {

  @Test
  void createsValidNotificationMessage() {
    NotificationMessage message = new NotificationMessage(
        "user@example.com",
        "Subject",
        "Body content",
        NotificationChannel.SMTP);

    assertEquals("user@example.com", message.recipient());
    assertEquals("Subject", message.subject());
    assertEquals("Body content", message.body());
    assertEquals(NotificationChannel.SMTP, message.channel());
  }

  @Test
  void acceptsSubjectAtMaximumAllowedLength() {
    String maxSubject = "A".repeat(200);
    assertDoesNotThrow(() ->
        new NotificationMessage("user@example.com", maxSubject, "Body", NotificationChannel.SMTP));
  }

  @Test
  void throwsExceptionWhenRecipientNullOrBlank() {
    assertThrows(NullPointerException.class, () ->
        new NotificationMessage(null, "Subject", "Body", NotificationChannel.SMTP));

    assertThrows(IllegalArgumentException.class, () ->
        new NotificationMessage("", "Subject", "Body", NotificationChannel.SMTP));

    assertThrows(IllegalArgumentException.class, () ->
        new NotificationMessage("   ", "Subject", "Body", NotificationChannel.SMTP));
  }

  @Test
  void throwsExceptionWhenRecipientInvalidEmail() {
    assertThrows(IllegalArgumentException.class, () ->
        new NotificationMessage("invalid-email", "Subject", "Body", NotificationChannel.SMTP));
  }

  @Test
  void throwsExceptionWhenSubjectNullOrBlankOrTooLong() {
    assertThrows(NullPointerException.class, () ->
        new NotificationMessage("user@example.com", null, "Body", NotificationChannel.SMTP));

    assertThrows(IllegalArgumentException.class, () ->
        new NotificationMessage("user@example.com", "", "Body", NotificationChannel.SMTP));

    assertThrows(IllegalArgumentException.class, () ->
        new NotificationMessage("user@example.com", "   ", "Body", NotificationChannel.SMTP));

    String tooLong = "A".repeat(201);
    assertThrows(IllegalArgumentException.class, () ->
        new NotificationMessage("user@example.com", tooLong, "Body", NotificationChannel.SMTP));
  }

  @Test
  void throwsExceptionWhenBodyNullOrBlank() {
    assertThrows(NullPointerException.class, () ->
        new NotificationMessage("user@example.com", "Subject", null, NotificationChannel.SMTP));

    assertThrows(IllegalArgumentException.class, () ->
        new NotificationMessage("user@example.com", "Subject", "", NotificationChannel.SMTP));

    assertThrows(IllegalArgumentException.class, () ->
        new NotificationMessage("user@example.com", "Subject", "   ", NotificationChannel.SMTP));
  }

  @Test
  void throwsExceptionWhenChannelNull() {
    assertThrows(NullPointerException.class, () ->
        new NotificationMessage("user@example.com", "Subject", "Body", null));
  }
}
