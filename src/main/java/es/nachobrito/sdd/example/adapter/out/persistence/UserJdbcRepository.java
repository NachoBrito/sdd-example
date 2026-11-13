package es.nachobrito.sdd.example.adapter.out.persistence;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Micronaut Data JDBC repository for {@link UserEntity}.
 */
@JdbcRepository(dialect = Dialect.MYSQL)
public interface UserJdbcRepository extends GenericRepository<UserEntity, UUID> {

  /**
   * Inserts a new user entity.
   *
   * @param entity the user entity
   * @return the inserted entity
   */
  UserEntity insert(UserEntity entity);

  /**
   * Checks whether a user exists with the given identifier.
   *
   * @param id the user identifier
   * @return true if present
   */
  boolean existsById(UUID id);

  /**
   * Checks whether a user exists with the given username.
   *
   * @param username the username
   * @return true if present
   */
  boolean existsByUsername(String username);

  /**
   * Finds a user entity by identifier.
   *
   * @param id the user identifier
   * @return the user entity if found
   */
  Optional<UserEntity> findById(UUID id);

  /**
   * Finds a user entity by username.
   *
   * @param username the username
   * @return the user entity if found
   */
  Optional<UserEntity> findByUsername(String username);

  /**
   * Inserts a role assignment for a user.
   *
   * @param userId the user identifier
   * @param role the role name
   */
  @Query("INSERT INTO user_roles (user_id, role) VALUES (:userId, :role)")
  void insertRole(UUID userId, String role);

  /**
   * Lists assigned role names for a user.
   *
   * @param userId the user identifier
   * @return the list of role names
   */
  @Query("SELECT role FROM user_roles WHERE user_id = :userId ORDER BY role")
  List<String> findRolesByUserId(UUID userId);
}

