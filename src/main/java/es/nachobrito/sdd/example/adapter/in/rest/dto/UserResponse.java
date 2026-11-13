package es.nachobrito.sdd.example.adapter.in.rest.dto;

import es.nachobrito.sdd.example.domain.model.Role;
import es.nachobrito.sdd.example.domain.model.User;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response body representing a registered user without exposing password hashes.
 *
 * @param userId the user identifier
 * @param username the username
 * @param roles the assigned roles
 * @param createdAt the creation timestamp in UTC
 */
@Serdeable
@Introspected
public record UserResponse(UUID userId, String username, List<String> roles, Instant createdAt) {

  /**
   * Compact constructor ensuring an unmodifiable roles list.
   */
  public UserResponse {
    roles = roles == null ? List.of() : List.copyOf(roles);
  }

  /**
   * Maps a domain {@link User} to a {@link UserResponse}.
   *
   * @param user the domain user
   * @return the response DTO
   */
  public static UserResponse fromDomain(User user) {
    return new UserResponse(
        user.id(),
        user.username(),
        user.roles().stream().map(Role::name).toList(),
        user.createdAt());
  }
}

