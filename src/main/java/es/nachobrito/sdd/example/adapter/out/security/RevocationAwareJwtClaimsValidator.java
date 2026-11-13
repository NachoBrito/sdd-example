package es.nachobrito.sdd.example.adapter.out.security;

import es.nachobrito.sdd.example.application.port.out.AuthTokenRepositoryPort;
import es.nachobrito.sdd.example.domain.model.AuthToken;
import es.nachobrito.sdd.example.domain.model.TokenType;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.token.Claims;
import io.micronaut.security.token.jwt.validator.GenericJwtClaimsValidator;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Claims validator that rejects revoked, expired, or non-access JWT tokens.
 */
@Singleton
public class RevocationAwareJwtClaimsValidator
    implements GenericJwtClaimsValidator<HttpRequest<?>> {

  private final AuthTokenRepositoryPort authTokenRepository;

  /**
   * Constructs the claims validator with the token repository.
   *
   * @param authTokenRepository the auth token repository port
   */
  public RevocationAwareJwtClaimsValidator(AuthTokenRepositoryPort authTokenRepository) {
    this.authTokenRepository = authTokenRepository;
  }

  @Override
  public boolean validate(Claims claims, HttpRequest<?> request) {
    if (request == null) {
      // Internal parseToken() validates signature and expiration; AuthService checks type and DB.
      return true;
    }
    Object jtiRaw = claims.get("jti");
    Object tokenTypeRaw = claims.get(JwtTokenIssuerAdapter.CLAIM_TOKEN_TYPE);
    if (jtiRaw == null || !TokenType.ACCESS.name().equals(String.valueOf(tokenTypeRaw))) {
      return false;
    }
    try {
      UUID tokenId = UUID.fromString(String.valueOf(jtiRaw));
      Optional<AuthToken> stored = authTokenRepository.findByTokenId(tokenId);
      return stored.isPresent() && stored.get().isActiveAt(Instant.now());
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }
}

