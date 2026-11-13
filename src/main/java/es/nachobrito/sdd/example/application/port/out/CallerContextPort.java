package es.nachobrito.sdd.example.application.port.out;

import es.nachobrito.sdd.example.domain.model.Role;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Outbound port that provides the authenticated caller identity and roles.
 */
@FunctionalInterface
public interface CallerContextPort {

  /**
   * Returns the current authenticated caller context if present.
   *
   * @return the caller context, or empty when no authenticated caller exists
   */
  Optional<CallerContext> currentCaller();

  /**
   * Immutable snapshot of the authenticated caller identity and roles.
   *
   * @param userId the caller user identifier
   * @param roles the assigned roles
   */
  record CallerContext(UUID userId, Set<Role> roles) {

    /**
     * Creates a validated caller context with an immutable role set.
     */
    public CallerContext {
      roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    /**
     * Returns an immutable copy of the caller roles.
     *
     * @return the caller roles
     */
    @Override
    public Set<Role> roles() {
      return Set.copyOf(roles);
    }

    /**
     * Checks whether the caller holds the specified role.
     *
     * @param role the role to check
     * @return true when the caller holds the role
     */
    public boolean hasRole(Role role) {
      return roles.contains(role);
    }
  }
}

