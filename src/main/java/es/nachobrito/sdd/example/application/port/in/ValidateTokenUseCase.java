package es.nachobrito.sdd.example.application.port.in;

import es.nachobrito.sdd.example.domain.model.Role;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Inbound port for validating an active JWT token.
 */
@FunctionalInterface
public interface ValidateTokenUseCase {

  /**
   * Validates the given JWT token against signature, expiration, and revocation state.
   *
   * @param token the JWT token string
   * @return the validated token details
   */
  TokenValidationResult validate(String token);

  /**
   * Validated token details.
   *
   * @param valid whether the token is active
   * @param userId the user identifier
   * @param username the username
   * @param roles the assigned roles
   * @param expiresAt the expiration timestamp
   */
  record TokenValidationResult(
      boolean valid, UUID userId, String username, List<Role> roles, Instant expiresAt) {

    /**
     * Compact constructor ensuring an unmodifiable roles list.
     */
    public TokenValidationResult {
      roles = roles == null ? List.of() : List.copyOf(roles);
    }
  }
}

