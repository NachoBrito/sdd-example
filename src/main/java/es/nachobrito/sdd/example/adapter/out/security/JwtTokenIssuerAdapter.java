package es.nachobrito.sdd.example.adapter.out.security;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import es.nachobrito.sdd.example.application.port.out.TokenIssuerPort;
import es.nachobrito.sdd.example.domain.model.Role;
import es.nachobrito.sdd.example.domain.model.TokenType;
import es.nachobrito.sdd.example.domain.model.User;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.token.jwt.generator.JwtTokenGenerator;
import io.micronaut.security.token.jwt.validator.JsonWebTokenValidator;
import jakarta.inject.Singleton;
import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Micronaut Security JWT implementation of {@link TokenIssuerPort}.
 */
@Singleton
public class JwtTokenIssuerAdapter implements TokenIssuerPort {

  static final String CLAIM_USER_ID = "userId";
  static final String CLAIM_ROLES = "roles";
  static final String CLAIM_TOKEN_TYPE = "tokenType";

  private final JwtTokenGenerator tokenGenerator;
  private final JsonWebTokenValidator<JWT, HttpRequest<?>> jwtValidator;

  /**
   * Constructs the adapter with Micronaut JWT generator and validator.
   *
   * @param tokenGenerator the Micronaut JWT token generator
   * @param jwtValidator the Micronaut JWT signature and expiration validator
   */
  public JwtTokenIssuerAdapter(
      JwtTokenGenerator tokenGenerator, JsonWebTokenValidator<JWT, HttpRequest<?>> jwtValidator) {
    this.tokenGenerator = tokenGenerator;
    this.jwtValidator = jwtValidator;
  }

  @Override
  public String issueToken(
      User user, UUID tokenId, TokenType tokenType, Instant issuedAt, Instant expiresAt) {
    Map<String, Object> claims = new ConcurrentHashMap<>();
    claims.put("sub", user.username());
    claims.put("jti", tokenId.toString());
    claims.put("iat", issuedAt.getEpochSecond());
    claims.put("exp", expiresAt.getEpochSecond());
    claims.put(CLAIM_USER_ID, user.id().toString());
    claims.put(CLAIM_ROLES, user.roles().stream().map(Role::name).toList());
    claims.put(CLAIM_TOKEN_TYPE, tokenType.name());
    return tokenGenerator
        .generateToken(claims)
        .orElseThrow(() -> new IllegalStateException("Failed to generate JWT token"));
  }

  @Override
  public Optional<ParsedTokenClaims> parseToken(String token) {
    if (token == null || token.isBlank()) {
      return Optional.empty();
    }
    Optional<JWT> validated = jwtValidator.validate(token, null);
    if (validated.isEmpty()) {
      return Optional.empty();
    }
    try {
      return toParsedClaims(validated.get().getJWTClaimsSet());
    } catch (ParseException | IllegalArgumentException ex) {
      return Optional.empty();
    }
  }

  private static Optional<ParsedTokenClaims> toParsedClaims(JWTClaimsSet claims)
      throws ParseException {
    String jti = claims.getJWTID();
    String userIdRaw = claims.getStringClaim(CLAIM_USER_ID);
    String username = claims.getSubject();
    String tokenTypeRaw = claims.getStringClaim(CLAIM_TOKEN_TYPE);
    List<String> roleStrings = claims.getStringListClaim(CLAIM_ROLES);
    Instant exp =
        claims.getExpirationTime() == null ? null : claims.getExpirationTime().toInstant();
    if (hasMissingClaim(jti, userIdRaw, username, tokenTypeRaw, roleStrings, exp)) {
      return Optional.empty();
    }
    List<Role> roles = roleStrings.stream().map(Role::valueOf).toList();
    return Optional.of(
        new ParsedTokenClaims(
            UUID.fromString(jti),
            UUID.fromString(userIdRaw),
            username,
            roles,
            TokenType.valueOf(tokenTypeRaw),
            exp));
  }

  private static boolean hasMissingClaim(
      String jti,
      String userIdRaw,
      String username,
      String tokenTypeRaw,
      List<String> roleStrings,
      Instant exp) {
    return jti == null
        || userIdRaw == null
        || username == null
        || tokenTypeRaw == null
        || roleStrings == null
        || exp == null;
  }
}
