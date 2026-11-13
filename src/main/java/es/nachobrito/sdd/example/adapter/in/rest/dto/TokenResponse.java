package es.nachobrito.sdd.example.adapter.in.rest.dto;

import es.nachobrito.sdd.example.application.port.in.LoginUseCase.IssuedTokens;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Response body containing issued access and refresh JWT tokens.
 *
 * @param accessToken the access JWT
 * @param refreshToken the refresh JWT
 * @param tokenType the token scheme
 * @param expiresInSeconds the access token lifetime in seconds
 */
@Serdeable
@Introspected
public record TokenResponse(
    String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {

  /**
   * Maps an {@link IssuedTokens} result into a {@link TokenResponse}.
   *
   * @param issued the issued tokens
   * @return the response DTO
   */
  public static TokenResponse fromIssued(IssuedTokens issued) {
    return new TokenResponse(
        issued.accessToken(), issued.refreshToken(), "Bearer", issued.expiresInSeconds());
  }
}

