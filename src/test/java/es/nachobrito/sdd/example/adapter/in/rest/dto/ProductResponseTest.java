package es.nachobrito.sdd.example.adapter.in.rest.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProductResponseTest {

  private static final UUID ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
  private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");

  @Test
  void fromDomainMapsProductFields() {
    Product product = Product.create(
        ID, "SKU-R", "Name R", "Desc R", 1_000, "USD", ProductStatus.ACTIVE, NOW);
    ProductResponse response = ProductResponse.fromDomain(product);

    assertEquals(ID, response.id());
  }
}

