package es.nachobrito.sdd.example.application.port.in;

import es.nachobrito.sdd.example.application.port.in.LoginUseCase.IssuedTokens;

/**
 * Inbound port for refreshing an active refresh token.
 */
@FunctionalInterface
public interface RefreshTokenUseCase {

  /**
   * Exchanges a valid, unrevoked refresh token for a new access and refresh token pair.
   *
   * @param refreshToken the refresh JWT string
   * @return the newly issued token pair
   */
  IssuedTokens refresh(String refreshToken);
}

