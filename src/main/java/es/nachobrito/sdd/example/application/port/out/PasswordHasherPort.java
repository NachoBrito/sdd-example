package es.nachobrito.sdd.example.application.port.out;

/**
 * Outbound port for hashing and verifying user passwords.
 */
public interface PasswordHasherPort {

  /**
   * Hashes a raw password for storage.
   *
   * @param rawPassword the plaintext password
   * @return the encoded password hash
   */
  String hash(String rawPassword);

  /**
   * Verifies a raw password against a stored password hash.
   *
   * @param rawPassword the plaintext password
   * @param storedHash the stored password hash
   * @return true when the password matches
   */
  boolean matches(String rawPassword, String storedHash);
}

