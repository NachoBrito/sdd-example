package es.nachobrito.sdd.example.adapter.out.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.application.port.out.CallerContextPort.CallerContext;
import es.nachobrito.sdd.example.domain.model.AuthenticationException;
import es.nachobrito.sdd.example.domain.model.Role;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.utils.SecurityService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MicronautSecurityCallerContextTest {

  private SecurityService securityService;
  private MicronautSecurityCallerContext adapter;

  @BeforeEach
  void setUp() {
    securityService = mock(SecurityService.class);
    adapter = new MicronautSecurityCallerContext(securityService);
  }

  @Test
  void returnsEmptyWhenUnauthenticated() {
    when(securityService.getAuthentication()).thenReturn(Optional.empty());
    assertTrue(adapter.currentCaller().isEmpty());
  }

  @Test
  void extractsUserIdAndDomainRoles() {
    UUID userId = UUID.randomUUID();
    Authentication auth =
        Authentication.build(
            "alice", List.of("CUSTOMER", "CUSTOM_ROLE"), Map.of("userId", userId.toString()));
    when(securityService.getAuthentication()).thenReturn(Optional.of(auth));

    Optional<CallerContext> caller = adapter.currentCaller();
    assertTrue(caller.isPresent());
    assertEquals(userId, caller.get().userId());
    assertEquals(Set.of(Role.CUSTOMER), caller.get().roles());
    assertTrue(caller.get().hasRole(Role.CUSTOMER));
    assertFalse(caller.get().hasRole(Role.ADMIN));
  }

  @Test
  void rejectsMissingOrInvalidUserIdClaim() {
    Authentication missing = Authentication.build("alice", List.of("CUSTOMER"), Map.of());
    when(securityService.getAuthentication()).thenReturn(Optional.of(missing));
    assertThrows(AuthenticationException.class, adapter::currentCaller);

    Authentication invalid =
        Authentication.build("alice", List.of("CUSTOMER"), Map.of("userId", "not-a-uuid"));
    when(securityService.getAuthentication()).thenReturn(Optional.of(invalid));
    assertThrows(AuthenticationException.class, adapter::currentCaller);

    CallerContext nullRoles = new CallerContext(UUID.randomUUID(), null);
    assertTrue(nullRoles.roles().isEmpty());
  }
}

