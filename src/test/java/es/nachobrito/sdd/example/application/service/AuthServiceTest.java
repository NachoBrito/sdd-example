package es.nachobrito.sdd.example.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.application.port.in.LoginUseCase.LoginCommand;
import es.nachobrito.sdd.example.application.port.in.RegisterUserUseCase.RegisterUserCommand;
import es.nachobrito.sdd.example.application.port.out.AuthTokenRepositoryPort;
import es.nachobrito.sdd.example.application.port.out.PasswordHasherPort;
import es.nachobrito.sdd.example.application.port.out.TokenIssuerPort;
import es.nachobrito.sdd.example.application.port.out.TokenIssuerPort.ParsedTokenClaims;
import es.nachobrito.sdd.example.application.port.out.UserRepositoryPort;
import es.nachobrito.sdd.example.domain.model.AuthToken;
import es.nachobrito.sdd.example.domain.model.AuthenticationException;
import es.nachobrito.sdd.example.domain.model.DuplicateUserIdentifierException;
import es.nachobrito.sdd.example.domain.model.DuplicateUsernameException;
import es.nachobrito.sdd.example.domain.model.Role;
import es.nachobrito.sdd.example.domain.model.TokenType;
import es.nachobrito.sdd.example.domain.model.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

  private UserRepositoryPort userRepository;
  private AuthTokenRepositoryPort authTokenRepository;
  private PasswordHasherPort passwordHasher;
  private TokenIssuerPort tokenIssuer;
  private AuthService service;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepositoryPort.class);
    authTokenRepository = mock(AuthTokenRepositoryPort.class);
    passwordHasher = mock(PasswordHasherPort.class);
    tokenIssuer = mock(TokenIssuerPort.class);
    service = new AuthService(
        userRepository, authTokenRepository, passwordHasher, tokenIssuer, 3_600L, 604_800L);
  }

  @Test
  void registersNewCustomerUser() {
    UUID userId = UUID.randomUUID();
    when(userRepository.existsById(userId)).thenReturn(false);
    when(userRepository.existsByUsername("alice")).thenReturn(false);
    when(passwordHasher.hash("SecretPass123!")).thenReturn("hashed-pw");
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    User created = service.register(new RegisterUserCommand(userId, " alice ", "SecretPass123!"));

    assertEquals(userId, created.id());
    assertEquals("alice", created.username());
    assertEquals("hashed-pw", created.passwordHash());
    assertEquals(List.of(Role.CUSTOMER), created.roles());
  }

  @Test
  void rejectsDuplicateUserIdOrUsernameOnRegister() {
    UUID userId = UUID.randomUUID();
    when(userRepository.existsById(userId)).thenReturn(true);

    assertThrows(
        DuplicateUserIdentifierException.class,
        () -> service.register(new RegisterUserCommand(userId, "alice", "SecretPass123!")));

    UUID userId2 = UUID.randomUUID();
    when(userRepository.existsById(userId2)).thenReturn(false);
    when(userRepository.existsByUsername("alice")).thenReturn(true);

    assertThrows(
        DuplicateUsernameException.class,
        () -> service.register(new RegisterUserCommand(userId2, "alice", "SecretPass123!")));
  }

  @Test
  void logsInAndIssuesTokenPair() {
    UUID userId = UUID.randomUUID();
    Instant now = Instant.now();
    User user = new User(userId, "alice", "hashed", List.of(Role.CUSTOMER), now, now);
    when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
    when(passwordHasher.matches("SecretPass123!", "hashed")).thenReturn(true);
    when(authTokenRepository.save(any(AuthToken.class))).thenAnswer(inv -> inv.getArgument(0));
    when(tokenIssuer.issueToken(any(), any(), any(), any(), any()))
        .thenReturn("jwt-access", "jwt-refresh");

    var tokens = service.login(new LoginCommand("alice", "SecretPass123!"));

    assertEquals("jwt-access", tokens.accessToken());
    assertEquals("jwt-refresh", tokens.refreshToken());
    assertEquals(3600L, tokens.expiresInSeconds());
    verify(authTokenRepository, times(2)).save(any(AuthToken.class));
  }

  @Test
  void rejectsInvalidCredentialsOnLogin() {
    when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
    assertThrows(
        AuthenticationException.class,
        () -> service.login(new LoginCommand("unknown", "pass")));
    assertThrows(
        AuthenticationException.class,
        () -> service.login(new LoginCommand(null, "pass")));

    UUID userId = UUID.randomUUID();
    Instant now = Instant.now();
    User user = new User(userId, "alice", "hashed", List.of(Role.CUSTOMER), now, now);
    when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
    when(passwordHasher.matches("wrong", "hashed")).thenReturn(false);

    assertThrows(
        AuthenticationException.class,
        () -> service.login(new LoginCommand("alice", "wrong")));
  }

  @Test
  void validatesRefreshesAndRevokesTokens() {
    UUID userId = UUID.randomUUID();
    UUID tokenId = UUID.randomUUID();
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(3600);
    User user = new User(userId, "alice", "hashed", List.of(Role.CUSTOMER), now, now);
    AuthToken stored = new AuthToken(tokenId, userId, TokenType.REFRESH, now, exp, null);
    ParsedTokenClaims claims =
        new ParsedTokenClaims(
            tokenId, userId, "alice", List.of(Role.CUSTOMER), TokenType.REFRESH, exp);

    when(tokenIssuer.parseToken("refresh-jwt")).thenReturn(Optional.of(claims));
    when(authTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(stored));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(authTokenRepository.update(any(AuthToken.class))).thenAnswer(inv -> inv.getArgument(0));
    when(authTokenRepository.save(any(AuthToken.class))).thenAnswer(inv -> inv.getArgument(0));
    when(tokenIssuer.issueToken(any(), any(), any(), any(), any()))
        .thenReturn("new-access", "new-refresh");

    var validation = service.validate("refresh-jwt");
    assertTrue(validation.valid());
    assertEquals(userId, validation.userId());

    var refreshed = service.refresh("refresh-jwt");
    assertEquals("new-access", refreshed.accessToken());

    service.revoke("refresh-jwt");
    verify(authTokenRepository, times(2)).update(any(AuthToken.class));
  }

  @Test
  void rejectsRevokedOrUnknownTokens() {
    UUID userId = UUID.randomUUID();
    UUID tokenId = UUID.randomUUID();
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(3600);
    final AuthToken revoked =
        new AuthToken(tokenId, userId, TokenType.ACCESS, now.minusSeconds(60), exp, now);
    ParsedTokenClaims claims =
        new ParsedTokenClaims(
            tokenId, userId, "alice", List.of(Role.CUSTOMER), TokenType.ACCESS, exp);

    when(tokenIssuer.parseToken("bad")).thenReturn(Optional.empty());
    assertThrows(AuthenticationException.class, () -> service.validate("bad"));

    when(tokenIssuer.parseToken("revoked")).thenReturn(Optional.of(claims));
    when(authTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(revoked));
    assertThrows(AuthenticationException.class, () -> service.validate("revoked"));
    assertNotNull(revoked.revokedAt());

    AuthToken activeAccess =
        new AuthToken(tokenId, userId, TokenType.ACCESS, now, exp, null);
    when(authTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(activeAccess));
    assertThrows(AuthenticationException.class, () -> service.refresh("revoked"));

    ParsedTokenClaims refreshClaims =
        new ParsedTokenClaims(
            tokenId, userId, "alice", List.of(Role.CUSTOMER), TokenType.REFRESH, exp);
    AuthToken activeRefresh =
        new AuthToken(tokenId, userId, TokenType.REFRESH, now, exp, null);
    when(tokenIssuer.parseToken("refresh-missing-user")).thenReturn(Optional.of(refreshClaims));
    when(authTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(activeRefresh));
    when(authTokenRepository.update(any(AuthToken.class))).thenAnswer(inv -> inv.getArgument(0));
    when(userRepository.findById(userId)).thenReturn(Optional.empty());
    assertThrows(AuthenticationException.class, () -> service.refresh("refresh-missing-user"));

    when(tokenIssuer.parseToken("unknown-token")).thenReturn(Optional.of(claims));
    when(authTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.empty());
    assertThrows(AuthenticationException.class, () -> service.revoke("unknown-token"));
  }
}

