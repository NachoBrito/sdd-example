package es.nachobrito.sdd.example.adapter.out.security;

import es.nachobrito.sdd.example.application.port.out.PasswordHasherPort;
import jakarta.inject.Singleton;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * PBKDF2-HMAC-SHA256 implementation of {@link PasswordHasherPort}.
 */
@Singleton
public class Pbkdf2PasswordHasher implements PasswordHasherPort {

  private static final String PREFIX = "pbkdf2-sha256";
  private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
  private static final int ITERATIONS = 310_000;
  private static final int SALT_BYTES = 16;
  private static final int KEY_BITS = 256;
  private static final int EXPECTED_PARTS = 4;

  private final SecureRandom secureRandom = new SecureRandom();

  @Override
  public String hash(String rawPassword) {
    byte[] salt = new byte[SALT_BYTES];
    secureRandom.nextBytes(salt);
    byte[] derived = derive(rawPassword.toCharArray(), salt, ITERATIONS);
    return PREFIX
        + "$"
        + ITERATIONS
        + "$"
        + Base64.getEncoder().encodeToString(salt)
        + "$"
        + Base64.getEncoder().encodeToString(derived);
  }

  @Override
  public boolean matches(String rawPassword, String storedHash) {
    if (rawPassword == null || storedHash == null) {
      return false;
    }
    String[] parts = storedHash.split("\\$");
    if (parts.length != EXPECTED_PARTS || !PREFIX.equals(parts[0])) {
      return false;
    }
    try {
      int iterations = Integer.parseInt(parts[1]);
      byte[] salt = Base64.getDecoder().decode(parts[2]);
      byte[] expected = Base64.getDecoder().decode(parts[3]);
      byte[] actual = derive(rawPassword.toCharArray(), salt, iterations);
      return MessageDigest.isEqual(expected, actual);
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }

  private static byte[] derive(char[] password, byte[] salt, int iterations) {
    PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
    try {
      SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
      return factory.generateSecret(spec).getEncoded();
    } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
      throw new IllegalStateException("PBKDF2 password hashing failed", ex);
    } finally {
      spec.clearPassword();
    }
  }
}

