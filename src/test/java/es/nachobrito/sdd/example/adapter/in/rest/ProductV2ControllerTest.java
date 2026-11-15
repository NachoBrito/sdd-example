package es.nachobrito.sdd.example.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.adapter.in.rest.dto.CreateProductV2Request;
import es.nachobrito.sdd.example.adapter.in.rest.dto.ProductResponse;
import es.nachobrito.sdd.example.adapter.in.rest.dto.UpdateProductV2Request;
import es.nachobrito.sdd.example.application.port.in.CreateProductUseCase.CreateProductCommand;
import es.nachobrito.sdd.example.application.port.in.ListProductsUseCase.ListProductsQuery;
import es.nachobrito.sdd.example.application.port.in.ProductUseCase;
import es.nachobrito.sdd.example.application.port.in.ResolveProductDiscountsUseCase;
import es.nachobrito.sdd.example.application.port.in.UpdateProductUseCase.UpdateProductCommand;
import es.nachobrito.sdd.example.domain.model.ActiveDiscount;
import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductStatus;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductV2ControllerTest {

  private static final UUID ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID CAT_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");
  private static final UUID CAMPAIGN_ID =
      UUID.fromString("99999999-9999-9999-9999-999999999999");
  private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");

  @Mock
  private ProductUseCase productUseCase;

  @Mock
  private ResolveProductDiscountsUseCase discountUseCase;

  private ProductV2Controller controller;

  @BeforeEach
  void setUp() {
    controller = new ProductV2Controller(productUseCase, discountUseCase);
  }

  @Test
  void createReturnsCreatedWithV2Location() {
    CreateProductV2Request request = new CreateProductV2Request(
        ID, "SKU-V2", "Product V2", "Desc", 1_500, "EUR", ProductStatus.ACTIVE, CAT_ID);
    Product product = Product.create(
        ID, "SKU-V2", "Product V2", "Desc", 1_500, "EUR", ProductStatus.ACTIVE, CAT_ID, NOW);
    when(productUseCase.createProduct(any(CreateProductCommand.class))).thenReturn(product);

    HttpResponse<ProductResponse> response = controller.create(request);

    assertEquals(HttpStatus.CREATED, response.getStatus());
    assertEquals("/v2/products/" + ID, response.getHeaders().get("Location"));
    assertEquals(CAT_ID, response.body().categoryId());
  }

  @Test
  void getAndUpdateAndDeleteAndListDelegateToProductUseCase() {
    Product product = Product.create(
        ID, "SKU-V2", "Product V2", "Desc", 1_500, "EUR", ProductStatus.ACTIVE, CAT_ID, NOW);
    when(productUseCase.getProduct(ID)).thenReturn(product);
    when(discountUseCase.resolveActiveDiscounts(product))
        .thenReturn(List.of(new ActiveDiscount(CAMPAIGN_ID, "V2 Promo", 10)));
    ProductResponse byId = controller.getById(ID);
    assertEquals(CAT_ID, byId.categoryId());
    assertEquals(1, byId.discounts().size());

    UpdateProductV2Request updateRequest = new UpdateProductV2Request(
        "SKU-V2-U", "Updated", "New Desc", 2_000, "USD", ProductStatus.DRAFT, CAT_ID);
    Product updated = Product.create(
        ID, "SKU-V2-U", "Updated", "New Desc", 2_000, "USD", ProductStatus.DRAFT, CAT_ID, NOW);
    when(productUseCase.updateProduct(any(UpdateProductCommand.class))).thenReturn(updated);
    assertEquals("SKU-V2-U", controller.update(ID, updateRequest).sku());

    when(productUseCase.listProducts(any(ListProductsQuery.class))).thenReturn(List.of(product));
    when(discountUseCase.resolveActiveDiscounts(List.of(product)))
        .thenReturn(Map.of(ID, List.of(new ActiveDiscount(CAMPAIGN_ID, "V2 Promo", 10))));
    List<ProductResponse> listed = controller.list(null, null, null, "name", "asc");
    assertEquals(1, listed.size());
    assertEquals(10, listed.getFirst().discounts().getFirst().discountPercent());

    HttpResponse<Void> deleteResponse = controller.delete(ID);
    assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatus());
    verify(productUseCase).deleteProduct(ID);
  }
}
