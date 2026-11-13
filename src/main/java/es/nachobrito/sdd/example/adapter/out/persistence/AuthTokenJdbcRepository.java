package es.nachobrito.sdd.example.adapter.out.persistence;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Micronaut Data JDBC repository for {@link AuthTokenEntity}.
 */
@JdbcRepository(dialect = Dialect.MYSQL)
public interface AuthTokenJdbcRepository extends GenericRepository<AuthTokenEntity, UUID> {

  /**
   * Inserts a new token entity.
   *
   * @param entity the token entity
   * @return the inserted entity
   */
  AuthTokenEntity insert(AuthTokenEntity entity);

  /**
   * Updates an existing token entity.
   *
   * @param entity the token entity
   * @return the updated entity
   */
  AuthTokenEntity update(AuthTokenEntity entity);

  /**
   * Finds a token entity by its identifier.
   *
   * @param tokenId the token identifier
   * @return the token entity if found
   */
  Optional<AuthTokenEntity> findByTokenId(UUID tokenId);
}

