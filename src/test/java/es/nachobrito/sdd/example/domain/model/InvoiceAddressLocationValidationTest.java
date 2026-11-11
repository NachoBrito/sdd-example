package es.nachobrito.sdd.example.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class InvoiceAddressLocationValidationTest {

  @Test
  void rejectOverlongComplement() {
    String longComp = "C".repeat(256);
    assertThrows(InvalidOrderDataException.class, () -> new InvoiceAddress(
        "Name", "VAT123", "123456", "Street", longComp, "City", "State", "12345", "Country"));
  }

  @Test
  void acceptNullComplement() {
    assertDoesNotThrow(() -> new InvoiceAddress(
        "Name", "VAT123", "123456", "Street", null, "City", "State", "12345", "Country"));
  }

  @Test
  void rejectNullCity() {
    assertThrows(InvalidOrderDataException.class, () -> new InvoiceAddress(
        "Name", "VAT123", "123456", "Street", null, null, "State", "12345", "Country"));
  }

  @Test
  void rejectOverlongCity() {
    String longCity = "C".repeat(101);
    assertThrows(InvalidOrderDataException.class, () -> new InvoiceAddress(
        "Name", "VAT123", "123456", "Street", null, longCity, "State", "12345", "Country"));
  }

  @Test
  void rejectNullState() {
    assertThrows(InvalidOrderDataException.class, () -> new InvoiceAddress(
        "Name", "VAT123", "123456", "Street", null, "City", null, "12345", "Country"));
  }

  @Test
  void rejectOverlongState() {
    String longState = "S".repeat(101);
    assertThrows(InvalidOrderDataException.class, () -> new InvoiceAddress(
        "Name", "VAT123", "123456", "Street", null, "City", longState, "12345", "Country"));
  }

  @Test
  void rejectNullZipCode() {
    assertThrows(InvalidOrderDataException.class, () -> new InvoiceAddress(
        "Name", "VAT123", "123456", "Street", null, "City", "State", null, "Country"));
  }

  @Test
  void rejectOverlongZipCode() {
    String longZip = "Z".repeat(21);
    assertThrows(InvalidOrderDataException.class, () -> new InvoiceAddress(
        "Name", "VAT123", "123456", "Street", null, "City", "State", longZip, "Country"));
  }

  @Test
  void rejectNullCountry() {
    assertThrows(InvalidOrderDataException.class, () -> new InvoiceAddress(
        "Name", "VAT123", "123456", "Street", null, "City", "State", "12345", null));
  }

  @Test
  void rejectOverlongCountry() {
    String longCountry = "C".repeat(101);
    assertThrows(InvalidOrderDataException.class, () -> new InvoiceAddress(
        "Name", "VAT123", "123456", "Street", null, "City", "State", "12345", longCountry));
  }
}

