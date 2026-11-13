package es.nachobrito.sdd.example.adapter.out.security;

import es.nachobrito.sdd.example.application.port.out.CallerContextPort;
import es.nachobrito.sdd.example.domain.model.AuthenticationException;
import es.nachobrito.sdd.example.domain.model.Role;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.utils.SecurityService;
import jakarta.inject.Singleton;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves the current authenticated caller from Micronaut Security.
 */
@Singleton
public class MicronautSecurityCallerContext implements CallerContextPort {

  private final SecurityService securityService;

  /**
   * Constructs the caller context adapter.
   *
   * @param securityService the Micronaut security service
   */
  public MicronautSecurityCallerContext(SecurityService securityService) {
    this.securityService = securityService;
  }

  @Override
  public Optional<CallerContext> currentCaller() {
    return securityService.getAuthentication().map(MicronautSecurityCallerContext::toCaller);
  }

  private static CallerContext toCaller(Authentication authentication) {
    UUID userId = extractUserId(authentication);
    Set<Role> roles = EnumSet.noneOf(Role.class);
    for (String roleName : authentication.getRoles()) {
      try {
        roles.add(Role.valueOf(roleName));
      } catch (IllegalArgumentException ignored) {
        // Ignore non-domain roles if present.
      }
    }
    return new CallerContext(userId, roles);
  }

  private static UUID extractUserId(Authentication authentication) {
    Object raw = authentication.getAttributes().get("userId");
    if (raw == null) {
      throw new AuthenticationException("Missing userId claim in authentication token");
    }
    try {
      return UUID.fromString(raw.toString());
    } catch (IllegalArgumentException ex) {
      throw new AuthenticationException("Invalid userId claim in authentication token", ex);
    }
  }
}

