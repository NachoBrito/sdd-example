package es.nachobrito.sdd.example.domain.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ProductValidationTest {

  private static final UUID VALID_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final String VALID_SKU = "KEYBOARD-01";
  private static final String VALID_NAME = "Mechanical Keyboard";
  private static final String VALID_DESC = "RGB gaming keyboard";
  private static final int VALID_PRICE = 9_999;
  private static final String VALID_CURRENCY = "USD";
  private static final ProductStatus VALID_STATUS = ProductStatus.DRAFT;
  private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void rejectBlankSku(String invalidSku) {
    assertThrows(
        InvalidProductDataException.class,
        () -> Product.create(
            VALID_ID, invalidSku, VALID_NAME, VALID_DESC,
            VALID_PRICE, VALID_CURRENCY, VALID_STATUS, NOW));
  }

  @Test
  void rejectOverlongSku() {
    String longSku = "A".repeat(101);
    assertThrows(
        InvalidProductDataException.class,
        () -> Product.create(
            VALID_ID, longSku, VALID_NAME, VALID_DESC,
            VALID_PRICE, VALID_CURRENCY, VALID_STATUS, NOW));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void rejectBlankName(String invalidName) {
    assertThrows(
        InvalidProductDataException.class,
        () -> Product.create(
            VALID_ID, VALID_SKU, invalidName, VALID_DESC,
            VALID_PRICE, VALID_CURRENCY, VALID_STATUS, NOW));
  }

  @Test
  void rejectOverlongName() {
    String longName = "A".repeat(256);
    assertThrows(
        InvalidProductDataException.class,
        () -> Product.create(
            VALID_ID, VALID_SKU, longName, VALID_DESC,
            VALID_PRICE, VALID_CURRENCY, VALID_STATUS, NOW));
  }

  @Test
  void rejectOverlongDescription() {
    String longDesc = "A".repeat(4_001);
    assertThrows(
        InvalidProductDataException.class,
        () -> Product.create(
            VALID_ID, VALID_SKU, VALID_NAME, longDesc,
            VALID_PRICE, VALID_CURRENCY, VALID_STATUS, NOW));
  }

  @Test
  void rejectNegativePrice() {
    assertThrows(
        InvalidProductDataException.class,
        () -> Product.create(
            VALID_ID, VALID_SKU, VALID_NAME, VALID_DESC,
            -1, VALID_CURRENCY, VALID_STATUS, NOW));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "  ", "US", "USDD", "INVALID"})
  void rejectInvalidCurrency(String invalidCurrency) {
    assertThrows(
        InvalidProductDataException.class,
        () -> Product.create(
            VALID_ID, VALID_SKU, VALID_NAME, VALID_DESC,
            VALID_PRICE, invalidCurrency, VALID_STATUS, NOW));
  }
}
