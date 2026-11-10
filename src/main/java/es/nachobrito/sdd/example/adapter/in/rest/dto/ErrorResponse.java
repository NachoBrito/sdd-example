package es.nachobrito.sdd.example.adapter.in.rest.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Collections;
import java.util.List;

/**
 * Standard error response model for REST API exceptions.
 *
 * @param message the primary error description
 * @param details optional list of specific violation messages
 */
@Serdeable
@Introspected
public record ErrorResponse(String message, List<String> details) {

  /**
   * Compact constructor that defensively copies the details list.
   */
  public ErrorResponse {
    details = details == null ? List.of() : List.copyOf(details);
  }

  /**
   * Constructs an ErrorResponse with a message and no extra details.
   *
   * @param message the error message
   */
  public ErrorResponse(String message) {
    this(message, List.of());
  }

  @Override
  public List<String> details() {
    return Collections.unmodifiableList(details);
  }
}
