package es.nachobrito.sdd.example.adapter.in.rest.dto;

import es.nachobrito.sdd.example.application.port.in.ValidateTokenUseCase.TokenValidationResult;
import es.nachobrito.sdd.example.domain.model.Role;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response body for token validation.
 *
 * @param valid whether the token is active
 * @param userId the user identifier
 * @param username the username
 * @param roles the assigned roles
 * @param expiresAt the expiration timestamp in UTC
 */
@Serdeable
@Introspected
public record TokenValidationResponse(
    boolean valid, UUID userId, String username, List<String> roles, Instant expiresAt) {

  /**
   * Compact constructor ensuring an unmodifiable roles list.
   */
  public TokenValidationResponse {
    roles = roles == null ? List.of() : List.copyOf(roles);
  }

  /**
   * Maps a {@link TokenValidationResult} into a {@link TokenValidationResponse}.
   *
   * @param result the validation result
   * @return the response DTO
   */
  public static TokenValidationResponse fromResult(TokenValidationResult result) {
    return new TokenValidationResponse(
        result.valid(),
        result.userId(),
        result.username(),
        result.roles().stream().map(Role::name).toList(),
        result.expiresAt());
  }
}

