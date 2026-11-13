package es.nachobrito.sdd.example.domain.model;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Domain entity representing an issued authentication token and its revocation state.
 *
 * @param tokenId the unique token identifier (jti)
 * @param userId the owning user identifier
 * @param tokenType the token type
 * @param issuedAt the issuance timestamp in UTC
 * @param expiresAt the expiration timestamp in UTC
 * @param revokedAt the revocation timestamp in UTC, or null when active
 */
public record AuthToken(
    UUID tokenId,
    UUID userId,
    TokenType tokenType,
    Instant issuedAt,
    Instant expiresAt,
    @Nullable Instant revokedAt) {

  /**
   * Compact constructor that validates all token invariants.
   */
  public AuthToken {
    if (tokenId == null) {
      throw new InvalidUserDataException("AuthToken tokenId cannot be null");
    }
    if (userId == null) {
      throw new InvalidUserDataException("AuthToken userId cannot be null");
    }
    if (tokenType == null) {
      throw new InvalidUserDataException("AuthToken tokenType cannot be null");
    }
    if (issuedAt == null) {
      throw new InvalidUserDataException("AuthToken issuedAt cannot be null");
    }
    if (expiresAt == null || !expiresAt.isAfter(issuedAt)) {
      throw new InvalidUserDataException("AuthToken expiresAt must be after issuedAt");
    }
  }

  /**
   * Checks whether this token is active at the given instant.
   *
   * @param now the evaluation timestamp
   * @return true when not revoked and not expired
   */
  public boolean isActiveAt(Instant now) {
    return revokedAt == null && now.isBefore(expiresAt);
  }

  /**
   * Returns a copy of this token marked as revoked at the given instant.
   *
   * @param now the revocation timestamp
   * @return the revoked token
   */
  public AuthToken revoke(Instant now) {
    if (now == null) {
      throw new InvalidUserDataException("Revocation timestamp cannot be null");
    }
    return new AuthToken(tokenId, userId, tokenType, issuedAt, expiresAt, now);
  }
}

