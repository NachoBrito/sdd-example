package es.nachobrito.sdd.example.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a notification message prepared for transport delivery.
 *
 * @param recipient destination customer email address
 * @param subject subject line of the notification
 * @param body message body content
 * @param channel target delivery channel
 */
public record NotificationMessage(
    String recipient,
    String subject,
    String body,
    NotificationChannel channel) {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

  private static final int MAX_SUBJECT_LENGTH = 200;

  /**
   * Validates message fields during record construction.
   */
  public NotificationMessage {
    Objects.requireNonNull(recipient, "Recipient must not be null");
    Objects.requireNonNull(subject, "Subject must not be null");
    Objects.requireNonNull(body, "Body must not be null");
    Objects.requireNonNull(channel, "Channel must not be null");

    if (recipient.isBlank()) {
      throw new IllegalArgumentException("Recipient must not be blank");
    }
    if (!EMAIL_PATTERN.matcher(recipient).matches()) {
      throw new IllegalArgumentException("Recipient has an invalid email format: " + recipient);
    }
    if (subject.isBlank()) {
      throw new IllegalArgumentException("Subject must not be blank");
    }
    if (subject.length() > MAX_SUBJECT_LENGTH) {
      throw new IllegalArgumentException(
          "Subject length exceeds maximum allowed characters: " + MAX_SUBJECT_LENGTH);
    }
    if (body.isBlank()) {
      throw new IllegalArgumentException("Body must not be blank");
    }
  }
}

