package es.nachobrito.sdd.example.adapter.out.persistence;

import es.nachobrito.sdd.example.application.port.out.UserRepositoryPort;
import es.nachobrito.sdd.example.domain.model.Role;
import es.nachobrito.sdd.example.domain.model.User;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC persistence adapter implementing {@link UserRepositoryPort}.
 */
@Singleton
public class UserPersistenceAdapter implements UserRepositoryPort {

  private final UserJdbcRepository userRepository;

  /**
   * Constructs the adapter with the JDBC repository.
   *
   * @param userRepository the user JDBC repository
   */
  public UserPersistenceAdapter(UserJdbcRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional
  public User save(User user) {
    UserEntity saved = userRepository.insert(UserEntity.fromDomain(user));
    for (Role role : user.roles()) {
      userRepository.insertRole(saved.id(), role.name());
    }
    return saved.toDomain(user.roles());
  }

  @Override
  public boolean existsById(UUID userId) {
    return userRepository.existsById(userId);
  }

  @Override
  public boolean existsByUsername(String username) {
    return userRepository.existsByUsername(username);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return userRepository.findByUsername(username).map(this::toDomainWithRoles);
  }

  @Override
  public Optional<User> findById(UUID userId) {
    return userRepository.findById(userId).map(this::toDomainWithRoles);
  }

  private User toDomainWithRoles(UserEntity entity) {
    List<Role> roles =
        userRepository.findRolesByUserId(entity.id()).stream().map(Role::valueOf).toList();
    return entity.toDomain(roles);
  }
}

