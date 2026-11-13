package es.nachobrito.sdd.example.application.port.out;

import es.nachobrito.sdd.example.domain.model.AuthToken;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound persistence port for authentication token metadata and revocation state.
 */
public interface AuthTokenRepositoryPort {

  /**
   * Persists a newly issued authentication token record.
   *
   * @param authToken the token record
   * @return the persisted token record
   */
  AuthToken save(AuthToken authToken);

  /**
   * Updates an existing authentication token record.
   *
   * @param authToken the updated token record
   * @return the updated token record
   */
  AuthToken update(AuthToken authToken);

  /**
   * Finds a token record by its unique identifier.
   *
   * @param tokenId the token identifier
   * @return the matching token record if present
   */
  Optional<AuthToken> findByTokenId(UUID tokenId);
}

