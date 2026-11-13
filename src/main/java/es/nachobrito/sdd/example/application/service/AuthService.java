package es.nachobrito.sdd.example.application.service;

import es.nachobrito.sdd.example.application.port.in.AuthUseCase;
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
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service implementing authentication and token lifecycle use cases.
 */
@Singleton
public class AuthService implements AuthUseCase {

  private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

  private final UserRepositoryPort userRepository;
  private final AuthTokenRepositoryPort authTokenRepository;
  private final PasswordHasherPort passwordHasher;
  private final TokenIssuerPort tokenIssuer;
  private final long accessExpirationSeconds;
  private final long refreshExpirationSeconds;

  /**
   * Constructs the authentication service.
   *
   * @param userRepository the user repository port
   * @param authTokenRepository the auth token repository port
   * @param passwordHasher the password hasher port
   * @param tokenIssuer the JWT issuer port
   * @param accessExpirationSeconds access token lifetime in seconds
   * @param refreshExpirationSeconds refresh token lifetime in seconds
   */
  public AuthService(
      UserRepositoryPort userRepository,
      AuthTokenRepositoryPort authTokenRepository,
      PasswordHasherPort passwordHasher,
      TokenIssuerPort tokenIssuer,
      @Value("${auth.token.access-expiration-seconds:3600}") long accessExpirationSeconds,
      @Value("${auth.token.refresh-expiration-seconds:604800}") long refreshExpirationSeconds) {
    this.userRepository = userRepository;
    this.authTokenRepository = authTokenRepository;
    this.passwordHasher = passwordHasher;
    this.tokenIssuer = tokenIssuer;
    this.accessExpirationSeconds = accessExpirationSeconds;
    this.refreshExpirationSeconds = refreshExpirationSeconds;
  }

  @Override
  public User register(RegisterUserCommand command) {
    String normalizedUsername = command.username() == null ? "" : command.username().trim();
    if (userRepository.existsById(command.userId())) {
      throw new DuplicateUserIdentifierException(command.userId());
    }
    if (userRepository.existsByUsername(normalizedUsername)) {
      throw new DuplicateUsernameException(normalizedUsername);
    }
    Instant now = Instant.now();
    String hashed = passwordHasher.hash(command.rawPassword());
    User user =
        new User(command.userId(), normalizedUsername, hashed, List.of(Role.CUSTOMER), now, now);
    User saved = userRepository.save(user);
    if (LOG.isInfoEnabled()) {
      LOG.info(
          "event=auth_register_succeeded user_id={} username={}",
          saved.id(),
          saved.username());
    }
    return saved;
  }

  @Override
  public IssuedTokens login(LoginCommand command) {
    String username = command.username() == null ? "" : command.username().trim();
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(
                () -> {
                  LOG.warn("event=auth_login_failed username={} reason=unknown_user", username);
                  return new AuthenticationException("Invalid username or password");
                });
    if (!passwordHasher.matches(command.password(), user.passwordHash())) {
      if (LOG.isWarnEnabled()) {
        LOG.warn(
            "event=auth_login_failed user_id={} username={} reason=invalid_password",
            user.id(),
            user.username());
      }
      throw new AuthenticationException("Invalid username or password");
    }
    IssuedTokens issued = issueTokenPair(user);
    if (LOG.isInfoEnabled()) {
      LOG.info("event=auth_login_succeeded user_id={} username={}", user.id(), user.username());
    }
    return issued;
  }

  @Override
  public TokenValidationResult validate(String token) {
    ParsedTokenClaims claims = parseActiveToken(token, null).claims();
    return new TokenValidationResult(
        true, claims.userId(), claims.username(), claims.roles(), claims.expiresAt());
  }

  @Override
  public IssuedTokens refresh(String refreshToken) {
    ActiveToken active = parseActiveToken(refreshToken, TokenType.REFRESH);
    Instant now = Instant.now();
    authTokenRepository.update(active.stored().revoke(now));

    User user =
        userRepository
            .findById(active.claims().userId())
            .orElseThrow(() -> new AuthenticationException("User no longer exists"));
    IssuedTokens issued = issueTokenPair(user);
    if (LOG.isInfoEnabled()) {
      LOG.info("event=auth_token_refreshed user_id={}", user.id());
    }
    return issued;
  }

  @Override
  public void revoke(String token) {
    ActiveToken active = parseActiveToken(token, null);
    authTokenRepository.update(active.stored().revoke(Instant.now()));
    if (LOG.isInfoEnabled()) {
      LOG.info(
          "event=auth_token_revoked user_id={} token_id={}",
          active.claims().userId(),
          active.claims().tokenId());
    }
  }

  private ActiveToken parseActiveToken(String token, TokenType expectedType) {
    ParsedTokenClaims claims =
        tokenIssuer
            .parseToken(token)
            .orElseThrow(() -> new AuthenticationException("Token is invalid or expired"));
    if (expectedType != null && claims.tokenType() != expectedType) {
      throw new AuthenticationException("Unexpected token type");
    }
    AuthToken stored =
        authTokenRepository
            .findByTokenId(claims.tokenId())
            .orElseThrow(() -> new AuthenticationException("Token is not recognized"));
    if (!stored.isActiveAt(Instant.now())) {
      throw new AuthenticationException("Token is expired or revoked");
    }
    return new ActiveToken(claims, stored);
  }

  private IssuedTokens issueTokenPair(User user) {
    Instant now = Instant.now();
    Instant accessExp = now.plusSeconds(accessExpirationSeconds);
    Instant refreshExp = now.plusSeconds(refreshExpirationSeconds);

    UUID accessId = UUID.randomUUID();
    UUID refreshId = UUID.randomUUID();

    authTokenRepository.save(
        new AuthToken(accessId, user.id(), TokenType.ACCESS, now, accessExp, null));
    authTokenRepository.save(
        new AuthToken(refreshId, user.id(), TokenType.REFRESH, now, refreshExp, null));

    String accessJwt = tokenIssuer.issueToken(user, accessId, TokenType.ACCESS, now, accessExp);
    String refreshJwt = tokenIssuer.issueToken(user, refreshId, TokenType.REFRESH, now, refreshExp);
    return new IssuedTokens(accessJwt, refreshJwt, accessExpirationSeconds);
  }

  private record ActiveToken(ParsedTokenClaims claims, AuthToken stored) {}
}

