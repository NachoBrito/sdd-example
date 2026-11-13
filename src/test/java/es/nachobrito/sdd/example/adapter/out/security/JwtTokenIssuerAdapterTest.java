package es.nachobrito.sdd.example.adapter.out.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import es.nachobrito.sdd.example.application.port.out.TokenIssuerPort.ParsedTokenClaims;
import es.nachobrito.sdd.example.domain.model.Role;
import es.nachobrito.sdd.example.domain.model.TokenType;
import es.nachobrito.sdd.example.domain.model.User;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.token.jwt.generator.JwtTokenGenerator;
import io.micronaut.security.token.jwt.validator.JsonWebTokenValidator;
import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenIssuerAdapterTest {

  private JwtTokenGenerator tokenGenerator;
  private JsonWebTokenValidator<JWT, HttpRequest<?>> jwtValidator;
  private JwtTokenIssuerAdapter adapter;

  @BeforeEach
  void setUp() {
    tokenGenerator = mock(JwtTokenGenerator.class);
    jwtValidator = mock();
    adapter = new JwtTokenIssuerAdapter(tokenGenerator, jwtValidator);
  }

  @Test
  void issueTokenGeneratesJwtAndThrowsWhenEmpty() {
    UUID userId = UUID.randomUUID();
    UUID tokenId = UUID.randomUUID();
    Instant now = Instant.now();
    User user = new User(userId, "alice", "hash", List.of(Role.CUSTOMER), now, now);

    when(tokenGenerator.generateToken(anyMap())).thenReturn(Optional.of("jwt-value"));
    assertEquals(
        "jwt-value",
        adapter.issueToken(user, tokenId, TokenType.ACCESS, now, now.plusSeconds(60)));

    when(tokenGenerator.generateToken(anyMap())).thenReturn(Optional.empty());
    assertThrows(
        IllegalStateException.class,
        () -> adapter.issueToken(user, tokenId, TokenType.ACCESS, now, now.plusSeconds(60)));
  }

  @Test
  void parseTokenHandlesValidAndInvalidInputs() throws ParseException {
    assertTrue(adapter.parseToken(null).isEmpty());
    assertTrue(adapter.parseToken("   ").isEmpty());

    when(jwtValidator.validate("invalid", null)).thenReturn(Optional.empty());
    assertTrue(adapter.parseToken("invalid").isEmpty());

    UUID userId = UUID.randomUUID();
    UUID tokenId = UUID.randomUUID();
    long expEpoch = Instant.now().plusSeconds(3_600).getEpochSecond();
    JWT validJwt = mock(JWT.class);
    JWTClaimsSet validClaims =
        new JWTClaimsSet.Builder()
            .jwtID(tokenId.toString())
            .subject("alice")
            .claim("exp", expEpoch)
            .claim(JwtTokenIssuerAdapter.CLAIM_USER_ID, userId.toString())
            .claim(JwtTokenIssuerAdapter.CLAIM_ROLES, List.of("CUSTOMER"))
            .claim(JwtTokenIssuerAdapter.CLAIM_TOKEN_TYPE, "ACCESS")
            .build();
    when(validJwt.getJWTClaimsSet()).thenReturn(validClaims);
    when(jwtValidator.validate("valid", null)).thenReturn(Optional.of(validJwt));

    Optional<ParsedTokenClaims> parsed = adapter.parseToken("valid");
    assertTrue(parsed.isPresent());
    assertEquals(tokenId, parsed.get().tokenId());
    assertEquals(userId, parsed.get().userId());
    assertEquals("alice", parsed.get().username());
    assertEquals(List.of(Role.CUSTOMER), parsed.get().roles());
    assertEquals(TokenType.ACCESS, parsed.get().tokenType());

    JWT missingClaimsJwt = mock(JWT.class);
    when(missingClaimsJwt.getJWTClaimsSet()).thenReturn(new JWTClaimsSet.Builder().build());
    when(jwtValidator.validate("missing", null)).thenReturn(Optional.of(missingClaimsJwt));
    assertTrue(adapter.parseToken("missing").isEmpty());

    JWT badUuidJwt = mock(JWT.class);
    when(badUuidJwt.getJWTClaimsSet())
        .thenReturn(
            new JWTClaimsSet.Builder()
                .jwtID("not-a-uuid")
                .subject("alice")
                .claim("exp", expEpoch)
                .claim(JwtTokenIssuerAdapter.CLAIM_USER_ID, userId.toString())
                .claim(JwtTokenIssuerAdapter.CLAIM_ROLES, List.of("CUSTOMER"))
                .claim(JwtTokenIssuerAdapter.CLAIM_TOKEN_TYPE, "ACCESS")
                .build());
    when(jwtValidator.validate("bad-uuid", null)).thenReturn(Optional.of(badUuidJwt));
    assertTrue(adapter.parseToken("bad-uuid").isEmpty());

    List<JWTClaimsSet> incompleteClaims =
        List.of(
            new JWTClaimsSet.Builder()
                .subject("alice")
                .claim("exp", expEpoch)
                .claim(JwtTokenIssuerAdapter.CLAIM_USER_ID, userId.toString())
                .claim(JwtTokenIssuerAdapter.CLAIM_ROLES, List.of("CUSTOMER"))
                .claim(JwtTokenIssuerAdapter.CLAIM_TOKEN_TYPE, "ACCESS")
                .build(),
            new JWTClaimsSet.Builder()
                .jwtID(tokenId.toString())
                .claim("exp", expEpoch)
                .claim(JwtTokenIssuerAdapter.CLAIM_USER_ID, userId.toString())
                .claim(JwtTokenIssuerAdapter.CLAIM_ROLES, List.of("CUSTOMER"))
                .claim(JwtTokenIssuerAdapter.CLAIM_TOKEN_TYPE, "ACCESS")
                .build(),
            new JWTClaimsSet.Builder()
                .jwtID(tokenId.toString())
                .subject("alice")
                .claim(JwtTokenIssuerAdapter.CLAIM_USER_ID, userId.toString())
                .claim(JwtTokenIssuerAdapter.CLAIM_ROLES, List.of("CUSTOMER"))
                .claim(JwtTokenIssuerAdapter.CLAIM_TOKEN_TYPE, "ACCESS")
                .build(),
            new JWTClaimsSet.Builder()
                .jwtID(tokenId.toString())
                .subject("alice")
                .claim("exp", expEpoch)
                .claim(JwtTokenIssuerAdapter.CLAIM_ROLES, List.of("CUSTOMER"))
                .claim(JwtTokenIssuerAdapter.CLAIM_TOKEN_TYPE, "ACCESS")
                .build(),
            new JWTClaimsSet.Builder()
                .jwtID(tokenId.toString())
                .subject("alice")
                .claim("exp", expEpoch)
                .claim(JwtTokenIssuerAdapter.CLAIM_USER_ID, userId.toString())
                .claim(JwtTokenIssuerAdapter.CLAIM_TOKEN_TYPE, "ACCESS")
                .build(),
            new JWTClaimsSet.Builder()
                .jwtID(tokenId.toString())
                .subject("alice")
                .claim("exp", expEpoch)
                .claim(JwtTokenIssuerAdapter.CLAIM_USER_ID, userId.toString())
                .claim(JwtTokenIssuerAdapter.CLAIM_ROLES, List.of("CUSTOMER"))
                .build());
    for (int i = 0; i < incompleteClaims.size(); i++) {
      JWT incomplete = mock(JWT.class);
      when(incomplete.getJWTClaimsSet()).thenReturn(incompleteClaims.get(i));
      when(jwtValidator.validate("incomplete-" + i, null)).thenReturn(Optional.of(incomplete));
      assertTrue(adapter.parseToken("incomplete-" + i).isEmpty());
    }
  }
}
