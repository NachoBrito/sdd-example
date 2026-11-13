package es.nachobrito.sdd.example.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.adapter.in.rest.dto.LoginRequest;
import es.nachobrito.sdd.example.adapter.in.rest.dto.RefreshTokenRequest;
import es.nachobrito.sdd.example.adapter.in.rest.dto.RegisterRequest;
import es.nachobrito.sdd.example.adapter.in.rest.dto.TokenRequest;
import es.nachobrito.sdd.example.adapter.in.rest.dto.TokenResponse;
import es.nachobrito.sdd.example.adapter.in.rest.dto.TokenValidationResponse;
import es.nachobrito.sdd.example.adapter.in.rest.dto.UserResponse;
import es.nachobrito.sdd.example.application.port.in.AuthUseCase;
import es.nachobrito.sdd.example.application.port.in.LoginUseCase.IssuedTokens;
import es.nachobrito.sdd.example.application.port.in.ValidateTokenUseCase.TokenValidationResult;
import es.nachobrito.sdd.example.domain.model.AuthenticationException;
import es.nachobrito.sdd.example.domain.model.Role;
import es.nachobrito.sdd.example.domain.model.User;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthControllerTest {

  private AuthUseCase authUseCase;
  private AuthController controller;

  @BeforeEach
  void setUp() {
    authUseCase = mock(AuthUseCase.class);
    controller = new AuthController(authUseCase);
  }

  @Test
  void registerLoginAndRefreshDelegateToUseCase() {
    UUID userId = UUID.randomUUID();
    Instant now = Instant.now();
    User user = new User(userId, "alice", "hash", List.of(Role.CUSTOMER), now, now);
    when(authUseCase.register(any())).thenReturn(user);

    HttpResponse<UserResponse> registerRes =
        controller.register(new RegisterRequest(userId, "alice", "password123"));
    assertEquals(HttpStatus.CREATED, registerRes.getStatus());
    assertEquals(userId, registerRes.body().userId());

    IssuedTokens tokens = new IssuedTokens("access-1", "refresh-1", 3600L);
    when(authUseCase.login(any())).thenReturn(tokens);
    TokenResponse loginRes = controller.login(new LoginRequest("alice", "password123"));
    assertEquals("access-1", loginRes.accessToken());

    when(authUseCase.refresh("refresh-1")).thenReturn(tokens);
    TokenResponse refreshRes = controller.refresh(new RefreshTokenRequest("refresh-1"));
    assertEquals("refresh-1", refreshRes.refreshToken());
  }

  @Test
  void validateAndRevokeResolveTokenFromHeaderOrBody() {
    UUID userId = UUID.randomUUID();
    Instant exp = Instant.now().plusSeconds(3600);
    TokenValidationResult result =
        new TokenValidationResult(true, userId, "alice", List.of(Role.CUSTOMER), exp);
    when(authUseCase.validate("body-jwt")).thenReturn(result);
    when(authUseCase.validate("header-jwt")).thenReturn(result);

    TokenValidationResponse fromBody =
        controller.validate(null, new TokenRequest(" body-jwt "));
    assertTrue(fromBody.valid());

    TokenValidationResponse fromHeader =
        controller.validate("Bearer header-jwt", new TokenRequest("  "));
    assertEquals("alice", fromHeader.username());

    HttpResponse<Void> revokeRes = controller.revoke("Bearer header-jwt", null);
    assertEquals(HttpStatus.NO_CONTENT, revokeRes.getStatus());
    verify(authUseCase).revoke("header-jwt");

    assertThrows(
        AuthenticationException.class,
        () -> controller.validate("Basic abc", null));
    assertThrows(
        AuthenticationException.class,
        () -> controller.validate(null, new TokenRequest(null)));
    assertThrows(
        AuthenticationException.class,
        () -> controller.revoke("Bearer   ", null));
  }
}

