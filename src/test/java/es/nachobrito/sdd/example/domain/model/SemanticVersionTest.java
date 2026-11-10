package es.nachobrito.sdd.example.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SemanticVersionTest {

  @Test
  void parseReadsAllParts() {
    assertEquals(new SemanticVersion(10, 0, 7), SemanticVersion.parse("10.0.7"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "1", "1.2", "1.2.3.4", "01.2.3", "1.2.x", "-1.2.3", " 1.2.3"})
  void parseRejectsInvalidText(String text) {
    assertThrows(IllegalArgumentException.class, () -> SemanticVersion.parse(text));
  }

  @ParameterizedTest
  @CsvSource({"-1,0,0", "0,-1,0", "0,0,-1"})
  void constructorRejectsNegativeParts(int major, int minor, int patch) {
    assertThrows(IllegalArgumentException.class, () -> new SemanticVersion(major, minor, patch));
  }

  @Test
  void constructorAcceptsZero() {
    assertEquals("0.0.0", new SemanticVersion(0, 0, 0).toString());
  }

  @ParameterizedTest
  @CsvSource({"1.2.3, 1.2.3", "1.2.4, 1.2.3", "1.3.0, 1.2.9"})
  void newerVersionWithSameMajorIsBackwardCompatible(String candidate, String current) {
    assertTrue(
        SemanticVersion.parse(candidate).isBackwardCompatibleWith(SemanticVersion.parse(current)));
  }

  @ParameterizedTest
  @CsvSource({"2.0.0, 1.9.9", "1.0.0, 2.0.0", "1.2.2, 1.2.3", "1.1.9, 1.2.0"})
  void otherVersionsAreNotBackwardCompatible(String candidate, String current) {
    assertFalse(
        SemanticVersion.parse(candidate).isBackwardCompatibleWith(SemanticVersion.parse(current)));
  }
}

