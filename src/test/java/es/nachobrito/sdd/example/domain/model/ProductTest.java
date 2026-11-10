package es.nachobrito.sdd.example.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProductTest {

  private static final UUID VALID_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final String VALID_SKU = "KEYBOARD-01";
  private static final String VALID_NAME = "Mechanical Keyboard";
  private static final String VALID_DESC = "RGB gaming keyboard";
  private static final int VALID_PRICE = 9_999;
  private static final String VALID_CURRENCY = "USD";
  private static final ProductStatus VALID_STATUS = ProductStatus.DRAFT;
  private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");

  @Test
  void createProductSetsExpectedId() {
    Product product = Product.create(
        VALID_ID, VALID_SKU, VALID_NAME, VALID_DESC,
        VALID_PRICE, VALID_CURRENCY, VALID_STATUS, NOW);
    assertEquals(VALID_ID, product.id());
  }

  @Test
  void createProductWithZeroPriceAcceptsZero() {
    Product product = Product.create(
        VALID_ID, VALID_SKU, VALID_NAME, null,
        0, "eur", ProductStatus.ACTIVE, NOW);
    assertEquals(0, product.priceInCents());
  }

  @Test
  void createProductAcceptsMaxBoundarySku() {
    String maxSku = "A".repeat(100);
    Product product = Product.create(
        VALID_ID, maxSku, VALID_NAME, VALID_DESC,
        VALID_PRICE, VALID_CURRENCY, VALID_STATUS, NOW);
    assertEquals(maxSku, product.sku());
  }

  @Test
  void createProductAcceptsMaxBoundaryName() {
    String maxName = "A".repeat(255);
    Product product = Product.create(
        VALID_ID, VALID_SKU, maxName, VALID_DESC,
        VALID_PRICE, VALID_CURRENCY, VALID_STATUS, NOW);
    assertEquals(maxName, product.name());
  }

  @Test
  void createProductAcceptsMaxBoundaryDescription() {
    String maxDesc = "A".repeat(4_000);
    Product product = Product.create(
        VALID_ID, VALID_SKU, VALID_NAME, maxDesc,
        VALID_PRICE, VALID_CURRENCY, VALID_STATUS, NOW);
    assertEquals(maxDesc, product.description());
  }

  @Test
  void withUpdatedAttributesModifiesSku() {
    Product original = Product.create(
        VALID_ID, VALID_SKU, VALID_NAME, VALID_DESC,
        VALID_PRICE, VALID_CURRENCY, VALID_STATUS, NOW);
    Instant later = NOW.plusSeconds(3_600);
    Product updated = original.withUpdatedAttributes(
        "NEW-SKU", "New Name", "New Desc", 12_345, "EUR", ProductStatus.ACTIVE, later);
    assertEquals("NEW-SKU", updated.sku());
  }

  @Test
  void withUpdatedAttributesRefreshesTimestamp() {
    Product original = Product.create(
        VALID_ID, VALID_SKU, VALID_NAME, VALID_DESC,
        VALID_PRICE, VALID_CURRENCY, VALID_STATUS, NOW);
    Instant later = NOW.plusSeconds(3_600);
    Product updated = original.withUpdatedAttributes(
        "NEW-SKU", "New Name", "New Desc", 12_345, "EUR", ProductStatus.ACTIVE, later);
    assertEquals(later, updated.updatedAt());
  }

  @Test
  void withUpdatedAttributesPreservesCreatedAt() {
    Product original = Product.create(
        VALID_ID, VALID_SKU, VALID_NAME, VALID_DESC,
        VALID_PRICE, VALID_CURRENCY, VALID_STATUS, NOW);
    Instant later = NOW.plusSeconds(3_600);
    Product updated = original.withUpdatedAttributes(
        "NEW-SKU", "New Name", "New Desc", 12_345, "EUR", ProductStatus.ACTIVE, later);
    assertEquals(NOW, updated.createdAt());
  }
}
