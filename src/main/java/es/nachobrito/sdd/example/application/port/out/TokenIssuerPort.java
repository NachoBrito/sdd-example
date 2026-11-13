package es.nachobrito.sdd.example.application.port.out;

import es.nachobrito.sdd.example.domain.model.Role;
import es.nachobrito.sdd.example.domain.model.TokenType;
import es.nachobrito.sdd.example.domain.model.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for generating and parsing signed JWT tokens.
 */
public interface TokenIssuerPort {

  /**
   * Generates a signed JWT string for the given user and token identifier.
   *
   * @param user the authenticated user
   * @param tokenId the unique token identifier
   * @param tokenType the token type
   * @param issuedAt the issuance timestamp
   * @param expiresAt the expiration timestamp
   * @return the signed JWT string
   */
  String issueToken(
      User user, UUID tokenId, TokenType tokenType, Instant issuedAt, Instant expiresAt);

  /**
   * Parses and validates the cryptographic signature and expiration of a JWT string.
   *
   * @param token the signed JWT string
   * @return the parsed token claims if valid
   */
  Optional<ParsedTokenClaims> parseToken(String token);

  /**
   * Extracted claims from a cryptographically valid JWT token.
   *
   * @param tokenId the unique token identifier
   * @param userId the user identifier
   * @param username the username
   * @param roles the assigned roles
   * @param tokenType the token type
   * @param expiresAt the expiration timestamp
   */
  record ParsedTokenClaims(
      UUID tokenId,
      UUID userId,
      String username,
      List<Role> roles,
      TokenType tokenType,
      Instant expiresAt) {

    /**
     * Compact constructor ensuring an unmodifiable roles list.
     */
    public ParsedTokenClaims {
      roles = roles == null ? List.of() : List.copyOf(roles);
    }
  }
}

