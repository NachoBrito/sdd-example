package es.nachobrito.sdd.example.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.domain.model.Role;
import es.nachobrito.sdd.example.domain.model.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserPersistenceAdapterTest {

  private UserJdbcRepository repository;
  private UserPersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    repository = mock(UserJdbcRepository.class);
    adapter = new UserPersistenceAdapter(repository);
  }

  @Test
  void saveFindAndExistsDelegateToRepository() {
    UUID userId = UUID.randomUUID();
    Instant now = Instant.now();
    User user = new User(userId, "alice", "hash", List.of(Role.CUSTOMER, Role.CATALOG), now, now);
    UserEntity entity = UserEntity.fromDomain(user);

    when(repository.insert(any(UserEntity.class))).thenReturn(entity);
    User saved = adapter.save(user);
    assertEquals(user, saved);
    verify(repository).insertRole(userId, "CUSTOMER");
    verify(repository).insertRole(userId, "CATALOG");

    when(repository.existsById(userId)).thenReturn(true);
    assertTrue(adapter.existsById(userId));

    when(repository.existsByUsername("alice")).thenReturn(true);
    assertTrue(adapter.existsByUsername("alice"));

    when(repository.findByUsername("alice")).thenReturn(Optional.of(entity));
    when(repository.findRolesByUserId(userId)).thenReturn(List.of("CUSTOMER", "CATALOG"));
    Optional<User> byName = adapter.findByUsername("alice");
    assertTrue(byName.isPresent());
    assertEquals(user, byName.get());

    when(repository.findById(userId)).thenReturn(Optional.of(entity));
    Optional<User> byId = adapter.findById(userId);
    assertTrue(byId.isPresent());
    assertEquals(user, byId.get());
  }
}

