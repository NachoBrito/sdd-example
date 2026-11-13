package es.nachobrito.sdd.example.application.port.in;

import es.nachobrito.sdd.example.domain.model.User;
import java.util.UUID;

/**
 * Inbound port for registering a new user account.
 */
@FunctionalInterface
public interface RegisterUserUseCase {

  /**
   * Registers a new customer user.
   *
   * @param command the registration command
   * @return the created user
   */
  User register(RegisterUserCommand command);

  /**
   * Command payload for user registration.
   *
   * @param userId the client-generated user identifier
   * @param username the unique username
   * @param rawPassword the plaintext password
   */
  record RegisterUserCommand(UUID userId, String username, String rawPassword) {}
}

