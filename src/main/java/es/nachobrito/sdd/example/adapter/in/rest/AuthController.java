package es.nachobrito.sdd.example.adapter.in.rest;

import es.nachobrito.sdd.example.adapter.in.rest.dto.LoginRequest;
import es.nachobrito.sdd.example.adapter.in.rest.dto.RefreshTokenRequest;
import es.nachobrito.sdd.example.adapter.in.rest.dto.RegisterRequest;
import es.nachobrito.sdd.example.adapter.in.rest.dto.TokenRequest;
import es.nachobrito.sdd.example.adapter.in.rest.dto.TokenResponse;
import es.nachobrito.sdd.example.adapter.in.rest.dto.TokenValidationResponse;
import es.nachobrito.sdd.example.adapter.in.rest.dto.UserResponse;
import es.nachobrito.sdd.example.application.port.in.AuthUseCase;
import es.nachobrito.sdd.example.application.port.in.LoginUseCase.IssuedTokens;
import es.nachobrito.sdd.example.application.port.in.LoginUseCase.LoginCommand;
import es.nachobrito.sdd.example.application.port.in.RegisterUserUseCase.RegisterUserCommand;
import es.nachobrito.sdd.example.application.port.in.ValidateTokenUseCase.TokenValidationResult;
import es.nachobrito.sdd.example.domain.model.AuthenticationException;
import es.nachobrito.sdd.example.domain.model.User;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.jspecify.annotations.Nullable;

/**
 * REST controller for user registration, login, and JWT token lifecycle operations.
 */
@Validated
@Controller("/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for authentication and token management")
public class AuthController {

  private static final String BEARER_PREFIX = "Bearer ";

  private final AuthUseCase authUseCase;

  /**
   * Constructs the controller with the authentication use case port.
   *
   * @param authUseCase the combined auth use case port
   */
  public AuthController(AuthUseCase authUseCase) {
    this.authUseCase = authUseCase;
  }

  /**
   * Registers a new customer user.
   *
   * @param request the registration request body
   * @return 201 Created with user details
   */
  @Post("/register")
  @Secured(SecurityRule.IS_ANONYMOUS)
  @Operation(summary = "Register user", description = "Creates a new customer account")
  @ApiResponse(responseCode = "201", description = "User created")
  @ApiResponse(responseCode = "400", description = "Validation error")
  @ApiResponse(responseCode = "409", description = "Duplicate userId or username")
  public HttpResponse<UserResponse> register(@Body @Valid RegisterRequest request) {
    User created =
        authUseCase.register(
            new RegisterUserCommand(request.userId(), request.username(), request.password()));
    return HttpResponse.created(
        UserResponse.fromDomain(created), URI.create("/v1/auth/users/" + created.id()));
  }

  /**
   * Authenticates a user and issues access and refresh JWT tokens.
   *
   * @param request the login credentials
   * @return 200 OK with issued JWT tokens
   */
  @Post("/login")
  @Secured(SecurityRule.IS_ANONYMOUS)
  @Operation(summary = "Login", description = "Exchanges user credentials for JWT tokens")
  @ApiResponse(responseCode = "200", description = "Authentication successful")
  @ApiResponse(responseCode = "401", description = "Invalid credentials")
  public TokenResponse login(@Body @Valid LoginRequest request) {
    IssuedTokens tokens =
        authUseCase.login(new LoginCommand(request.username(), request.password()));
    return TokenResponse.fromIssued(tokens);
  }

  /**
   * Validates an active JWT token.
   *
   * @param authorization the Authorization header
   * @param request optional request body containing a token
   * @return 200 OK with token claims and validity
   */
  @Post("/validate")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Operation(summary = "Validate token", description = "Validates a JWT token")
  @ApiResponse(responseCode = "200", description = "Token is valid")
  @ApiResponse(responseCode = "401", description = "Token is missing, invalid, or revoked")
  public TokenValidationResponse validate(
      @Nullable @Header(HttpHeaders.AUTHORIZATION) String authorization,
      @Nullable @Body TokenRequest request) {
    String token = resolveToken(authorization, request);
    TokenValidationResult result = authUseCase.validate(token);
    return TokenValidationResponse.fromResult(result);
  }

  /**
   * Refreshes an active JWT token pair.
   *
   * @param request the refresh token request
   * @return 200 OK with new access and refresh tokens
   */
  @Post("/refresh")
  @Secured(SecurityRule.IS_ANONYMOUS)
  @Operation(summary = "Refresh token", description = "Rotates a refresh token for new JWT tokens")
  @ApiResponse(responseCode = "200", description = "Token refreshed")
  @ApiResponse(responseCode = "401", description = "Refresh token is invalid, expired, or revoked")
  public TokenResponse refresh(@Body @Valid RefreshTokenRequest request) {
    IssuedTokens tokens = authUseCase.refresh(request.refreshToken());
    return TokenResponse.fromIssued(tokens);
  }

  /**
   * Revokes an active JWT token.
   *
   * @param authorization the Authorization header
   * @param request optional request body containing a token
   * @return 204 No Content when revoked
   */
  @Post("/revoke")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Operation(summary = "Revoke token", description = "Revokes a JWT token immediately")
  @ApiResponse(responseCode = "204", description = "Token revoked")
  @ApiResponse(responseCode = "401", description = "Token is missing, invalid, or revoked")
  public HttpResponse<Void> revoke(
      @Nullable @Header(HttpHeaders.AUTHORIZATION) String authorization,
      @Nullable @Body TokenRequest request) {
    String token = resolveToken(authorization, request);
    authUseCase.revoke(token);
    return HttpResponse.noContent();
  }

  private static String resolveToken(
      @Nullable String authorization, @Nullable TokenRequest request) {
    String bodyToken = request == null ? null : request.token();
    if (bodyToken != null && !bodyToken.isBlank()) {
      return bodyToken.trim();
    }
    if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
      String extracted = authorization.substring(BEARER_PREFIX.length()).trim();
      if (!extracted.isEmpty()) {
        return extracted;
      }
    }
    throw new AuthenticationException("Missing JWT token");
  }
}

