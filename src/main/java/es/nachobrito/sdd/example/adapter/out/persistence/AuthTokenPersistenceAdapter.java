package es.nachobrito.sdd.example.adapter.out.persistence;

import es.nachobrito.sdd.example.application.port.out.AuthTokenRepositoryPort;
import es.nachobrito.sdd.example.domain.model.AuthToken;
import jakarta.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC persistence adapter implementing {@link AuthTokenRepositoryPort}.
 */
@Singleton
public class AuthTokenPersistenceAdapter implements AuthTokenRepositoryPort {

  private final AuthTokenJdbcRepository repository;

  /**
   * Constructs the adapter with the JDBC repository.
   *
   * @param repository the auth token JDBC repository
   */
  public AuthTokenPersistenceAdapter(AuthTokenJdbcRepository repository) {
    this.repository = repository;
  }

  @Override
  public AuthToken save(AuthToken authToken) {
    return repository.insert(AuthTokenEntity.fromDomain(authToken)).toDomain();
  }

  @Override
  public AuthToken update(AuthToken authToken) {
    return repository.update(AuthTokenEntity.fromDomain(authToken)).toDomain();
  }

  @Override
  public Optional<AuthToken> findByTokenId(UUID tokenId) {
    return repository.findByTokenId(tokenId).map(AuthTokenEntity::toDomain);
  }
}

