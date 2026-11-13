package es.nachobrito.sdd.example.domain.model;

import java.util.UUID;

/**
 * Thrown when attempting to register a user with an identifier that already exists.
 */
public class DuplicateUserIdentifierException extends AuthException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception for the duplicated user identifier.
   *
   * @param userId the conflicting user identifier
   */
  public DuplicateUserIdentifierException(UUID userId) {
    super("User already exists with identifier: " + userId);
  }
}

