package es.nachobrito.sdd.example.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.adapter.in.rest.dto.CreateProductRequest;
import es.nachobrito.sdd.example.adapter.in.rest.dto.ProductResponse;
import es.nachobrito.sdd.example.adapter.in.rest.dto.UpdateProductRequest;
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
class ProductControllerTest {

  @Mock
  private ProductUseCase productUseCase;

  @Mock
  private ResolveProductDiscountsUseCase discountUseCase;

  private ProductController controller;

  private static final UUID ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID CAMPAIGN_ID =
      UUID.fromString("99999999-9999-9999-9999-999999999999");
  private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");

  @BeforeEach
  void setUp() {
    controller = new ProductController(productUseCase, discountUseCase);
  }

  @Test
  void createReturnsCreatedResponse() {
    CreateProductRequest request = new CreateProductRequest(
        ID, "SKU-1", "Product 1", "Desc 1", 1_000, "USD", ProductStatus.ACTIVE, null);
    Product product = Product.create(
        ID, "SKU-1", "Product 1", "Desc 1", 1_000, "USD", ProductStatus.ACTIVE, NOW);
    when(productUseCase.createProduct(any(CreateProductCommand.class))).thenReturn(product);

    HttpResponse<ProductResponse> response = controller.create(request);

    assertEquals(HttpStatus.CREATED, response.getStatus());
  }

  @Test
  void getByIdReturnsProductResponseWithActiveDiscounts() {
    Product product = Product.create(
        ID, "SKU-1", "Product 1", "Desc 1", 1_000, "USD", ProductStatus.ACTIVE, NOW);
    when(productUseCase.getProduct(ID)).thenReturn(product);
    when(discountUseCase.resolveActiveDiscounts(product))
        .thenReturn(List.of(new ActiveDiscount(CAMPAIGN_ID, "Flash", 20)));

    ProductResponse response = controller.getById(ID);

    assertEquals("SKU-1", response.sku());
    assertEquals(1, response.discounts().size());
    assertEquals(20, response.discounts().getFirst().discountPercent());
  }

  @Test
  void updateReturnsUpdatedProductResponse() {
    UpdateProductRequest request = new UpdateProductRequest(
        "SKU-2", "Product 2", "Desc 2", 2_000, "EUR", ProductStatus.DRAFT, null);
    Product product = Product.create(
        ID, "SKU-2", "Product 2", "Desc 2", 2_000, "EUR", ProductStatus.DRAFT, NOW);
    when(productUseCase.updateProduct(any(UpdateProductCommand.class))).thenReturn(product);

    ProductResponse response = controller.update(ID, request);

    assertEquals("SKU-2", response.sku());
  }

  @Test
  void deleteReturnsNoContentResponse() {
    HttpResponse<Void> response = controller.delete(ID);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
  }

  @Test
  void deleteDelegatesToUseCase() {
    controller.delete(ID);

    verify(productUseCase).deleteProduct(ID);
  }

  @Test
  void listReturnsListOfProductResponses() {
    Product product = Product.create(
        ID, "SKU-1", "Product 1", "Desc 1", 1_000, "USD", ProductStatus.ACTIVE, NOW);
    when(productUseCase.listProducts(any(ListProductsQuery.class))).thenReturn(List.of(product));
    when(discountUseCase.resolveActiveDiscounts(List.of(product)))
        .thenReturn(Map.of(ID, List.of(new ActiveDiscount(CAMPAIGN_ID, "Flash", 15))));

    List<ProductResponse> responses =
        controller.list(null, null, null, "name", "asc");

    assertEquals(1, responses.size());
    assertEquals(1, responses.getFirst().discounts().size());
  }
}
