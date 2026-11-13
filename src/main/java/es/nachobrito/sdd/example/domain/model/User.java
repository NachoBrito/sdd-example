package es.nachobrito.sdd.example.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing an authenticated user.
 *
 * @param id the client-generated unique user identifier
 * @param username the unique username
 * @param passwordHash the hashed password
 * @param roles the non-empty list of assigned roles
 * @param createdAt the record creation timestamp in UTC
 * @param updatedAt the record last modification timestamp in UTC
 */
public record User(
    UUID id,
    String username,
    String passwordHash,
    List<Role> roles,
    Instant createdAt,
    Instant updatedAt) {

  private static final int MAX_USERNAME_LENGTH = 100;

  /**
   * Compact constructor that validates all user invariants.
   */
  public User {
    if (id == null) {
      throw new InvalidUserDataException("User id cannot be null");
    }
    if (username == null || username.isBlank()) {
      throw new InvalidUserDataException("User username cannot be blank");
    }
    username = username.trim();
    if (username.length() > MAX_USERNAME_LENGTH) {
      throw new InvalidUserDataException(
          "User username cannot exceed " + MAX_USERNAME_LENGTH + " characters");
    }
    if (passwordHash == null || passwordHash.isBlank()) {
      throw new InvalidUserDataException("User passwordHash cannot be blank");
    }
    if (roles == null || roles.isEmpty() || roles.stream().anyMatch(Objects::isNull)) {
      throw new InvalidUserDataException("User roles must contain at least one valid role");
    }
    roles = List.copyOf(roles);
    if (createdAt == null) {
      throw new InvalidUserDataException("User createdAt cannot be null");
    }
    if (updatedAt == null) {
      throw new InvalidUserDataException("User updatedAt cannot be null");
    }
  }
}

