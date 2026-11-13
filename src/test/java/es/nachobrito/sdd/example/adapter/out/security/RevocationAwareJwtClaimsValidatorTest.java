package es.nachobrito.sdd.example.adapter.out.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.application.port.out.AuthTokenRepositoryPort;
import es.nachobrito.sdd.example.domain.model.AuthToken;
import es.nachobrito.sdd.example.domain.model.TokenType;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.token.Claims;
import io.micronaut.security.token.MapClaims;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevocationAwareJwtClaimsValidatorTest {

  private AuthTokenRepositoryPort authTokenRepository;
  private RevocationAwareJwtClaimsValidator validator;
  private HttpRequest<?> request;

  @BeforeEach
  void setUp() {
    authTokenRepository = mock(AuthTokenRepositoryPort.class);
    validator = new RevocationAwareJwtClaimsValidator(authTokenRepository);
    request = mock(HttpRequest.class);
  }

  @Test
  void returnsTrueForInternalValidationWhenRequestIsNull() {
    Claims claims = new MapClaims(Map.of());
    assertTrue(validator.validate(claims, null));
  }

  @Test
  void rejectsMissingJtiOrNonAccessTokenType() {
    assertFalse(validator.validate(new MapClaims(Map.of()), request));
    Claims refreshClaims =
        new MapClaims(
            Map.of(
                "jti", UUID.randomUUID().toString(),
                JwtTokenIssuerAdapter.CLAIM_TOKEN_TYPE, "REFRESH"));
    assertFalse(validator.validate(refreshClaims, request));

    Claims badJtiClaims =
        new MapClaims(
            Map.of(
                "jti", "invalid-uuid",
                JwtTokenIssuerAdapter.CLAIM_TOKEN_TYPE, "ACCESS"));
    assertFalse(validator.validate(badJtiClaims, request));
  }

  @Test
  void validatesActiveTokenAndRejectsRevokedOrUnknown() {
    UUID tokenId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Instant now = Instant.now();
    Claims accessClaims =
        new MapClaims(
            Map.of(
                "jti", tokenId.toString(),
                JwtTokenIssuerAdapter.CLAIM_TOKEN_TYPE, "ACCESS"));

    when(authTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.empty());
    assertFalse(validator.validate(accessClaims, request));

    AuthToken revoked =
        new AuthToken(
            tokenId, userId, TokenType.ACCESS, now.minusSeconds(60), now.plusSeconds(600), now);
    when(authTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(revoked));
    assertFalse(validator.validate(accessClaims, request));

    AuthToken active =
        new AuthToken(tokenId, userId, TokenType.ACCESS, now, now.plusSeconds(600), null);
    when(authTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(active));
    assertTrue(validator.validate(accessClaims, request));
  }
}

