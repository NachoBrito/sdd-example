package es.nachobrito.sdd.example.application.port.out;

import es.nachobrito.sdd.example.domain.model.User;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound persistence port for user accounts.
 */
public interface UserRepositoryPort {

  /**
   * Saves a new user account.
   *
   * @param user the user domain entity
   * @return the persisted user
   */
  User save(User user);

  /**
   * Checks whether a user exists with the given identifier.
   *
   * @param userId the user identifier
   * @return true if a user exists
   */
  boolean existsById(UUID userId);

  /**
   * Checks whether a user exists with the given username.
   *
   * @param username the username
   * @return true if a user exists
   */
  boolean existsByUsername(String username);

  /**
   * Finds a user by username.
   *
   * @param username the username
   * @return the matching user if present
   */
  Optional<User> findByUsername(String username);

  /**
   * Finds a user by identifier.
   *
   * @param userId the user identifier
   * @return the matching user if present
   */
  Optional<User> findById(UUID userId);
}

