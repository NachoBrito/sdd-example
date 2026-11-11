package es.nachobrito.sdd.example.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DeliveryAddressValidationTest {

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void rejectBlankName(String invalid) {
    assertThrows(InvalidOrderDataException.class, () -> new DeliveryAddress(
        invalid, "123456", "Street", null, "City", "State", "12345", "Country"));
  }

  @Test
  void rejectNullName() {
    assertThrows(InvalidOrderDataException.class, () -> new DeliveryAddress(
        null, "123456", "Street", null, "City", "State", "12345", "Country"));
  }

  @Test
  void rejectOverlongName() {
    String longName = "A".repeat(256);
    assertThrows(InvalidOrderDataException.class, () -> new DeliveryAddress(
        longName, "123456", "Street", null, "City", "State", "12345", "Country"));
  }

  @Test
  void acceptExactMaxLengthName() {
    String maxName = "A".repeat(255);
    assertDoesNotThrow(() -> new DeliveryAddress(
        maxName, "123456", "Street", null, "City", "State", "12345", "Country"));
  }

  @Test
  void rejectNullTelephone() {
    assertThrows(InvalidOrderDataException.class, () -> new DeliveryAddress(
        "Name", null, "Street", null, "City", "State", "12345", "Country"));
  }

  @Test
  void rejectOverlongTelephone() {
    String longTel = "1".repeat(51);
    assertThrows(InvalidOrderDataException.class, () -> new DeliveryAddress(
        "Name", longTel, "Street", null, "City", "State", "12345", "Country"));
  }

  @Test
  void rejectNullStreet() {
    assertThrows(InvalidOrderDataException.class, () -> new DeliveryAddress(
        "Name", "123456", null, null, "City", "State", "12345", "Country"));
  }

  @Test
  void rejectOverlongStreet() {
    String longStreet = "S".repeat(256);
    assertThrows(InvalidOrderDataException.class, () -> new DeliveryAddress(
        "Name", "123456", longStreet, null, "City", "State", "12345", "Country"));
  }

  @Test
  void rejectOverlongComplement() {
    String longComp = "C".repeat(256);
    assertThrows(InvalidOrderDataException.class, () -> new DeliveryAddress(
        "Name", "123456", "Street", longComp, "City", "State", "12345", "Country"));
  }

  @Test
  void acceptNullComplement() {
    assertDoesNotThrow(() -> new DeliveryAddress(
        "Name", "123456", "Street", null, "City", "State", "12345", "Country"));
  }
}
