package es.nachobrito.sdd.example.application.port.in;

/**
 * Inbound port for revoking an active JWT token.
 */
@FunctionalInterface
public interface RevokeTokenUseCase {

  /**
   * Revokes the given JWT token so it can no longer be used or refreshed.
   *
   * @param token the JWT token string
   */
  void revoke(String token);
}

