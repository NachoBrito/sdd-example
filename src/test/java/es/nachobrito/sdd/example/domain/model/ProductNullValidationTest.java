package es.nachobrito.sdd.example.domain.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProductNullValidationTest {

  private static final UUID VALID_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final String VALID_SKU = "KEYBOARD-01";
  private static final String VALID_NAME = "Mechanical Keyboard";
  private static final String VALID_DESC = "RGB gaming keyboard";
  private static final int VALID_PRICE = 9_999;
  private static final String VALID_CURRENCY = "USD";
  private static final ProductStatus VALID_STATUS = ProductStatus.DRAFT;
  private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");

  @Test
  void rejectNullId() {
    assertThrows(
        InvalidProductDataException.class,
        () -> new Product(
            null, VALID_SKU, VALID_NAME, VALID_DESC,
            VALID_PRICE, VALID_CURRENCY, VALID_STATUS, NOW, NOW));
  }

  @Test
  void rejectNullSku() {
    assertThrows(
        InvalidProductDataException.class,
        () -> Product.create(
            VALID_ID, null, VALID_NAME, VALID_DESC,
            VALID_PRICE, VALID_CURRENCY, VALID_STATUS, NOW));
  }

  @Test
  void rejectNullName() {
    assertThrows(
        InvalidProductDataException.class,
        () -> Product.create(
            VALID_ID, VALID_SKU, null, VALID_DESC,
            VALID_PRICE, VALID_CURRENCY, VALID_STATUS, NOW));
  }

  @Test
  void rejectNullCurrency() {
    assertThrows(
        InvalidProductDataException.class,
        () -> Product.create(
            VALID_ID, VALID_SKU, VALID_NAME, VALID_DESC,
            VALID_PRICE, null, VALID_STATUS, NOW));
  }

  @Test
  void rejectNullStatus() {
    assertThrows(
        InvalidProductDataException.class,
        () -> Product.create(
            VALID_ID, VALID_SKU, VALID_NAME, VALID_DESC,
            VALID_PRICE, VALID_CURRENCY, null, NOW));
  }

  @Test
  void rejectNullCreatedAt() {
    assertThrows(
        InvalidProductDataException.class,
        () -> new Product(
            VALID_ID, VALID_SKU, VALID_NAME, VALID_DESC,
            VALID_PRICE, VALID_CURRENCY, VALID_STATUS, null, NOW));
  }

  @Test
  void rejectNullUpdatedAt() {
    assertThrows(
        InvalidProductDataException.class,
        () -> new Product(
            VALID_ID, VALID_SKU, VALID_NAME, VALID_DESC,
            VALID_PRICE, VALID_CURRENCY, VALID_STATUS, NOW, null));
  }
}

