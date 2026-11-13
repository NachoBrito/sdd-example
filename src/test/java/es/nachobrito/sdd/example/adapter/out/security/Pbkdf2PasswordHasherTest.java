package es.nachobrito.sdd.example.adapter.out.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Pbkdf2PasswordHasherTest {

  private final Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();

  @Test
  void hashesAndVerifiesPasswordsWithUniqueSalts() {
    String hash1 = hasher.hash("secret-password");
    String hash2 = hasher.hash("secret-password");

    assertNotEquals("secret-password", hash1);
    assertNotEquals(hash1, hash2);
    assertTrue(hasher.matches("secret-password", hash1));
    assertTrue(hasher.matches("secret-password", hash2));
    assertFalse(hasher.matches("wrong-password", hash1));
  }

  @Test
  void rejectsMalformedOrNullHashes() {
    assertFalse(hasher.matches(null, "pbkdf2-sha256$310000$aa$bb"));
    assertFalse(hasher.matches("secret", null));
    assertFalse(hasher.matches("secret", "not-a-valid-hash"));
    assertFalse(hasher.matches("secret", "bcrypt$310000$aa$bb"));
    assertFalse(hasher.matches("secret", "pbkdf2-sha256$invalid$aa$bb"));
  }
}

