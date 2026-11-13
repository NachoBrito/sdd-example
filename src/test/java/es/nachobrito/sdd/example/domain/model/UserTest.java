package es.nachobrito.sdd.example.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserTest {

  private static final Instant NOW = Instant.parse("2026-11-13T10:00:00Z");

  @Test
  void createsValidUserWithTrimmedUsernameAndImmutableRoles() {
    UUID id = UUID.randomUUID();
    List<Role> roles = new ArrayList<>();
    roles.add(Role.CUSTOMER);

    User user = new User(id, "  alice  ", "hashed", roles, NOW, NOW);

    assertEquals(id, user.id());
    assertEquals("alice", user.username());
    assertEquals(List.of(Role.CUSTOMER), user.roles());
    assertThrows(UnsupportedOperationException.class, () -> user.roles().add(Role.ADMIN));
  }

  @Test
  void rejectsInvalidUserFields() {
    final UUID id = UUID.randomUUID();
    final String tooLong = "a".repeat(101);

    User maxLen = new User(id, "a".repeat(100), "hash", List.of(Role.CUSTOMER), NOW, NOW);
    assertEquals(100, maxLen.username().length());

    assertThrows(
        InvalidUserDataException.class,
        () -> new User(null, "alice", "hash", List.of(Role.CUSTOMER), NOW, NOW));
    assertThrows(
        InvalidUserDataException.class,
        () -> new User(id, null, "hash", List.of(Role.CUSTOMER), NOW, NOW));
    assertThrows(
        InvalidUserDataException.class,
        () -> new User(id, "   ", "hash", List.of(Role.CUSTOMER), NOW, NOW));
    assertThrows(
        InvalidUserDataException.class,
        () -> new User(id, tooLong, "hash", List.of(Role.CUSTOMER), NOW, NOW));
    assertThrows(
        InvalidUserDataException.class,
        () -> new User(id, "alice", null, List.of(Role.CUSTOMER), NOW, NOW));
    assertThrows(
        InvalidUserDataException.class,
        () -> new User(id, "alice", " ", List.of(Role.CUSTOMER), NOW, NOW));
    assertThrows(
        InvalidUserDataException.class,
        () -> new User(id, "alice", "hash", null, NOW, NOW));
    assertThrows(
        InvalidUserDataException.class,
        () -> new User(id, "alice", "hash", List.of(), NOW, NOW));
    assertThrows(
        InvalidUserDataException.class,
        () -> new User(id, "alice", "hash", Arrays.asList(Role.CUSTOMER, null), NOW, NOW));
    assertThrows(
        InvalidUserDataException.class,
        () -> new User(id, "alice", "hash", List.of(Role.CUSTOMER), null, NOW));
    assertThrows(
        InvalidUserDataException.class,
        () -> new User(id, "alice", "hash", List.of(Role.CUSTOMER), NOW, null));
  }

  @Test
  void evaluatesAndRevokesAuthToken() {
    UUID tokenId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Instant exp = NOW.plusSeconds(3600);
    AuthToken token = new AuthToken(tokenId, userId, TokenType.ACCESS, NOW, exp, null);

    assertTrue(token.isActiveAt(NOW.plusSeconds(10)));
    assertFalse(token.isActiveAt(exp));

    AuthToken revoked = token.revoke(NOW.plusSeconds(30));
    assertNotNull(revoked.revokedAt());
    assertFalse(revoked.isActiveAt(NOW.plusSeconds(31)));
  }

  @Test
  void rejectsInvalidAuthTokenFields() {
    UUID tokenId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Instant exp = NOW.plusSeconds(60);

    assertThrows(
        InvalidUserDataException.class,
        () -> new AuthToken(null, userId, TokenType.ACCESS, NOW, exp, null));
    assertThrows(
        InvalidUserDataException.class,
        () -> new AuthToken(tokenId, null, TokenType.ACCESS, NOW, exp, null));
    assertThrows(
        InvalidUserDataException.class,
        () -> new AuthToken(tokenId, userId, null, NOW, exp, null));
    assertThrows(
        InvalidUserDataException.class,
        () -> new AuthToken(tokenId, userId, TokenType.ACCESS, null, exp, null));
    assertThrows(
        InvalidUserDataException.class,
        () -> new AuthToken(tokenId, userId, TokenType.ACCESS, NOW, null, null));
    assertThrows(
        InvalidUserDataException.class,
        () -> new AuthToken(tokenId, userId, TokenType.ACCESS, NOW, NOW, null));

    AuthToken valid = new AuthToken(tokenId, userId, TokenType.ACCESS, NOW, exp, null);
    assertThrows(InvalidUserDataException.class, () -> assertNotNull(valid.revoke(null)));
  }
}

