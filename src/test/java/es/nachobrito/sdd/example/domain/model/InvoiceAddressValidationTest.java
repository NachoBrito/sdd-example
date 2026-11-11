package es.nachobrito.sdd.example.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class InvoiceAddressValidationTest {

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void rejectBlankName(String invalid) {
    assertThrows(InvalidOrderDataException.class, () -> new InvoiceAddress(
        invalid, "VAT123", "123456", "Street", null, "City", "State", "12345", "Country"));
  }

  @Test
  void rejectNullName() {
    assertThrows(InvalidOrderDataException.class, () -> new InvoiceAddress(
        null, "VAT123", "123456", "Street", null, "City", "State", "12345", "Country"));
  }

  @Test
  void rejectOverlongName() {
    String longName = "A".repeat(256);
    assertThrows(InvalidOrderDataException.class, () -> new InvoiceAddress(
        longName, "VAT123", "123456", "Street", null, "City", "State", "12345", "Country"));
  }

  @Test
  void acceptExactMaxLengthName() {
    String maxName = "A".repeat(255);
    assertDoesNotThrow(() -> new InvoiceAddress(
        maxName, "VAT123", "123456", "Street", null, "City", "State", "12345", "Country"));
  }

  @Test
  void rejectNullVatNumber() {
    assertThrows(InvalidOrderDataException.class, () -> new InvoiceAddress(
        "Name", null, "123456", "Street", null, "City", "State", "12345", "Country"));
  }

  @Test
  void rejectOverlongVatNumber() {
    String longVat = "V".repeat(51);
    assertThrows(InvalidOrderDataException.class, () -> new InvoiceAddress(
        "Name", longVat, "123456", "Street", null, "City", "State", "12345", "Country"));
  }

  @Test
  void rejectNullTelephone() {
    assertThrows(InvalidOrderDataException.class, () -> new InvoiceAddress(
        "Name", "VAT123", null, "Street", null, "City", "State", "12345", "Country"));
  }

  @Test
  void rejectOverlongTelephone() {
    String longTel = "1".repeat(51);
    assertThrows(InvalidOrderDataException.class, () -> new InvoiceAddress(
        "Name", "VAT123", longTel, "Street", null, "City", "State", "12345", "Country"));
  }

  @Test
  void rejectNullStreet() {
    assertThrows(InvalidOrderDataException.class, () -> new InvoiceAddress(
        "Name", "VAT123", "123456", null, null, "City", "State", "12345", "Country"));
  }

  @Test
  void rejectOverlongStreet() {
    String longStreet = "S".repeat(256);
    assertThrows(InvalidOrderDataException.class, () -> new InvoiceAddress(
        "Name", "VAT123", "123456", longStreet, null, "City", "State", "12345", "Country"));
  }
}
