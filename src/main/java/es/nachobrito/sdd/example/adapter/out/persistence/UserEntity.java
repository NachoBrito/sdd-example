package es.nachobrito.sdd.example.adapter.out.persistence;

import es.nachobrito.sdd.example.domain.model.Role;
import es.nachobrito.sdd.example.domain.model.User;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Persistence entity mapping the users table.
 *
 * @param id the user primary key
 * @param username the unique username
 * @param passwordHash the hashed password
 * @param createdAt the creation timestamp
 * @param updatedAt the update timestamp
 */
@MappedEntity("users")
public record UserEntity(
    @Id @NonNull UUID id,
    @NonNull @MappedProperty("username") String username,
    @NonNull @MappedProperty("password_hash") String passwordHash,
    @NonNull @MappedProperty("created_at") Instant createdAt,
    @NonNull @MappedProperty("updated_at") Instant updatedAt) {

  /**
   * Creates a persistence entity from a domain user.
   *
   * @param user the domain user
   * @return the persistence entity
   */
  public static UserEntity fromDomain(User user) {
    return new UserEntity(
        user.id(), user.username(), user.passwordHash(), user.createdAt(), user.updatedAt());
  }

  /**
   * Converts this persistence entity and its roles into a domain user.
   *
   * @param roles the assigned domain roles
   * @return the domain user
   */
  public User toDomain(List<Role> roles) {
    return new User(id, username, passwordHash, roles, createdAt, updatedAt);
  }
}

