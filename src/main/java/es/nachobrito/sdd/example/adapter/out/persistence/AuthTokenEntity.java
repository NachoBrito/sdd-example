package es.nachobrito.sdd.example.adapter.out.persistence;

import es.nachobrito.sdd.example.domain.model.AuthToken;
import es.nachobrito.sdd.example.domain.model.TokenType;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import java.time.Instant;
import java.util.UUID;

/**
 * Persistence entity mapping the auth_tokens table.
 *
 * @param tokenId the token primary key (jti)
 * @param userId the owning user identifier
 * @param tokenType the token type
 * @param issuedAt the issuance timestamp
 * @param expiresAt the expiration timestamp
 * @param revokedAt the revocation timestamp, or null when active
 */
@MappedEntity("auth_tokens")
public record AuthTokenEntity(
    @Id @NonNull @MappedProperty("token_id") UUID tokenId,
    @NonNull @MappedProperty("user_id") UUID userId,
    @NonNull @MappedProperty("token_type") String tokenType,
    @NonNull @MappedProperty("issued_at") Instant issuedAt,
    @NonNull @MappedProperty("expires_at") Instant expiresAt,
    @Nullable @MappedProperty("revoked_at") Instant revokedAt) {

  /**
   * Creates a persistence entity from a domain {@link AuthToken}.
   *
   * @param authToken the domain token
   * @return the persistence entity
   */
  public static AuthTokenEntity fromDomain(AuthToken authToken) {
    return new AuthTokenEntity(
        authToken.tokenId(),
        authToken.userId(),
        authToken.tokenType().name(),
        authToken.issuedAt(),
        authToken.expiresAt(),
        authToken.revokedAt());
  }

  /**
   * Converts this persistence entity into a domain {@link AuthToken}.
   *
   * @return the domain token
   */
  public AuthToken toDomain() {
    return new AuthToken(
        tokenId, userId, TokenType.valueOf(tokenType), issuedAt, expiresAt, revokedAt);
  }
}

