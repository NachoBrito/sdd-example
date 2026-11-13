package es.nachobrito.sdd.example.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.domain.model.AuthToken;
import es.nachobrito.sdd.example.domain.model.TokenType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthTokenPersistenceAdapterTest {

  private AuthTokenJdbcRepository repository;
  private AuthTokenPersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    repository = mock(AuthTokenJdbcRepository.class);
    adapter = new AuthTokenPersistenceAdapter(repository);
  }

  @Test
  void saveUpdateAndFindByTokenIdDelegateToRepository() {
    UUID tokenId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Instant now = Instant.now();
    AuthToken token =
        new AuthToken(tokenId, userId, TokenType.ACCESS, now, now.plusSeconds(3600), null);
    AuthTokenEntity entity = AuthTokenEntity.fromDomain(token);

    when(repository.insert(any(AuthTokenEntity.class))).thenReturn(entity);
    assertEquals(token, adapter.save(token));

    AuthToken revoked = token.revoke(now);
    AuthTokenEntity revokedEntity = AuthTokenEntity.fromDomain(revoked);
    when(repository.update(any(AuthTokenEntity.class))).thenReturn(revokedEntity);
    assertEquals(revoked, adapter.update(revoked));

    when(repository.findByTokenId(tokenId)).thenReturn(Optional.of(entity));
    Optional<AuthToken> found = adapter.findByTokenId(tokenId);
    assertTrue(found.isPresent());
    assertEquals(token, found.get());
  }
}

