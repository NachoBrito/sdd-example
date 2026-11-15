package es.nachobrito.sdd.example.adapter.in.rest.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.nachobrito.sdd.example.domain.model.ActiveDiscount;
import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProductResponseTest {

  private static final UUID ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
  private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");

  @Test
  void fromDomainMapsProductFieldsAndDiscountsImmutably() {
    UUID categoryId = UUID.fromString("77777777-7777-7777-7777-777777777777");
    Product product = Product.create(
        ID, "SKU-R", "Name R", "Desc R", 1_000, "USD", ProductStatus.ACTIVE, categoryId, NOW);
    ProductResponse response = ProductResponse.fromDomain(product);

    assertEquals(ID, response.id());
    assertEquals(categoryId, response.categoryId());
    assertTrue(response.discounts().isEmpty());

    UUID campaignId = UUID.fromString("88888888-8888-8888-8888-888888888888");
    ProductDiscountResponse discountDto = new ProductDiscountResponse(campaignId, "Sale", 20);
    ProductResponse withDiscounts = new ProductResponse(
        ID, "SKU-R", "Name R", "Desc R", 1_000, "USD",
        ProductStatus.ACTIVE, categoryId, NOW, NOW, null);
    assertTrue(withDiscounts.discounts().isEmpty());

    List<ProductResponse> list = ProductResponse.fromDomainList(
        List.of(product),
        Map.of(ID, List.of(new ActiveDiscount(campaignId, "Sale", 20))));
    assertEquals(1, list.getFirst().discounts().size());
    assertThrows(
        UnsupportedOperationException.class,
        () -> list.getFirst().discounts().add(discountDto));
    assertThrows(NullPointerException.class, () -> ProductDiscountResponse.fromDomain(null));
    assertThrows(NullPointerException.class, () -> ProductResponse.fromDomain(null));
  }
}

