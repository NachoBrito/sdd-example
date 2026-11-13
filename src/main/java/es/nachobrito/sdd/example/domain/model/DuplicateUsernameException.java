package es.nachobrito.sdd.example.domain.model;

/**
 * Thrown when attempting to register a user with a username that already exists.
 */
public class DuplicateUsernameException extends AuthException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception for the duplicated username.
   *
   * @param username the conflicting username
   */
  public DuplicateUsernameException(String username) {
    super("User already exists with username: " + username);
  }
}

