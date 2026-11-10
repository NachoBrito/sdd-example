package es.nachobrito.sdd.example.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProductEntityTest {

  private static final UUID ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
  private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");

  @Test
  void fromDomainMapsAllFieldsCorrectly() {
    Product product = Product.create(
        ID, "SKU-E", "Name E", "Desc E", 5_000, "USD", ProductStatus.ACTIVE, NOW);
    ProductEntity entity = ProductEntity.fromDomain(product);

    assertEquals("SKU-E", entity.sku());
  }

  @Test
  void toDomainMapsAllFieldsCorrectly() {
    ProductEntity entity = new ProductEntity(
        ID, "SKU-E", "Name E", "Desc E", 5_000, "USD", "ACTIVE", NOW, NOW);
    Product product = entity.toDomain();

    assertEquals(ProductStatus.ACTIVE, product.status());
  }
}

