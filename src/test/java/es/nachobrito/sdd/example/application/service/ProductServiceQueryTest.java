package es.nachobrito.sdd.example.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.application.port.in.ListProductsUseCase.ListProductsQuery;
import es.nachobrito.sdd.example.application.port.out.ProductRepositoryPort;
import es.nachobrito.sdd.example.domain.model.InvalidProductDataException;
import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductNotFoundException;
import es.nachobrito.sdd.example.domain.model.ProductStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceQueryTest {

  @Mock
  private ProductRepositoryPort repository;

  private ProductService service;
  private Instant now;

  private static final UUID ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final String SKU = "PROD-1";
  private static final String NAME = "Laptop Stand";
  private static final String DESC = "Aluminum laptop stand";
  private static final int PRICE = 4_999;
  private static final String CURRENCY = "USD";
  private static final ProductStatus STATUS = ProductStatus.DRAFT;

  @BeforeEach
  void setUp() {
    now = Instant.parse("2026-11-10T10:00:00Z");
    Clock fixedClock = Clock.fixed(now, ZoneOffset.UTC);
    service = new ProductService(repository, fixedClock);
  }

  @Test
  void getProductReturnsExistingProduct() {
    Product product = Product.create(ID, SKU, NAME, DESC, PRICE, CURRENCY, STATUS, now);
    when(repository.findById(ID)).thenReturn(Optional.of(product));

    Product result = service.getProduct(ID);

    assertEquals(product, result);
  }

  @Test
  void getProductThrowsWhenNotFound() {
    when(repository.findById(ID)).thenReturn(Optional.empty());

    assertThrows(ProductNotFoundException.class, () -> service.getProduct(ID));
  }

  @Test
  void listProductsPassesParametersToRepository() {
    ListProductsQuery query = new ListProductsQuery(
        "SKU", ".*Laptop.*", ProductStatus.ACTIVE, "name", "asc");
    List<Product> products = List.of(
        Product.create(ID, SKU, NAME, DESC, PRICE, CURRENCY, STATUS, now));
    when(repository.findAll("SKU", ".*Laptop.*", ProductStatus.ACTIVE, "name", "asc"))
        .thenReturn(products);

    List<Product> result = service.listProducts(query);

    assertEquals(products, result);
  }

  @Test
  void listProductsAcceptsBlankRegex() {
    ListProductsQuery query = new ListProductsQuery(
        null, "   ", null, "name", "asc");
    when(repository.findAll(null, "   ", null, "name", "asc"))
        .thenReturn(List.of());

    List<Product> result = service.listProducts(query);

    assertEquals(List.of(), result);
  }

  @Test
  void listProductsRejectsInvalidRegex() {
    ListProductsQuery query = new ListProductsQuery(
        null, "[invalid regex", null, "name", "asc");

    assertThrows(InvalidProductDataException.class, () -> service.listProducts(query));
  }
}

