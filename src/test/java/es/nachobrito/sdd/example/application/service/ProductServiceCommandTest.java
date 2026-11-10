package es.nachobrito.sdd.example.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.application.port.in.CreateProductUseCase.CreateProductCommand;
import es.nachobrito.sdd.example.application.port.in.UpdateProductUseCase.UpdateProductCommand;
import es.nachobrito.sdd.example.application.port.out.ProductRepositoryPort;
import es.nachobrito.sdd.example.domain.model.DuplicateIdentifierException;
import es.nachobrito.sdd.example.domain.model.DuplicateSkuException;
import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductNotFoundException;
import es.nachobrito.sdd.example.domain.model.ProductStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceCommandTest {

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
  void createProductSavesAndReturnsProduct() {
    CreateProductCommand command = new CreateProductCommand(
        ID, SKU, NAME, DESC, PRICE, CURRENCY, STATUS);
    Product expected = Product.create(ID, SKU, NAME, DESC, PRICE, CURRENCY, STATUS, now);
    when(repository.existsById(ID)).thenReturn(false);
    when(repository.existsBySku(SKU)).thenReturn(false);
    when(repository.save(any(Product.class))).thenReturn(expected);

    Product actual = service.createProduct(command);

    assertEquals(expected, actual);
  }

  @Test
  void createProductRejectsDuplicateId() {
    CreateProductCommand command = new CreateProductCommand(
        ID, SKU, NAME, DESC, PRICE, CURRENCY, STATUS);
    when(repository.existsById(ID)).thenReturn(true);

    assertThrows(DuplicateIdentifierException.class, () -> service.createProduct(command));
  }

  @Test
  void createProductRejectsDuplicateSku() {
    CreateProductCommand command = new CreateProductCommand(
        ID, SKU, NAME, DESC, PRICE, CURRENCY, STATUS);
    when(repository.existsById(ID)).thenReturn(false);
    when(repository.existsBySku(SKU)).thenReturn(true);

    assertThrows(DuplicateSkuException.class, () -> service.createProduct(command));
  }

  @Test
  void updateProductModifiesAttributes() {
    Product existing = Product.create(ID, SKU, NAME, DESC, PRICE, CURRENCY, STATUS, now);
    when(repository.findById(ID)).thenReturn(Optional.of(existing));
    when(repository.existsBySkuAndIdNot("NEW-SKU", ID)).thenReturn(false);

    UpdateProductCommand command = new UpdateProductCommand(
        ID, "NEW-SKU", "New Name", "New Desc", 5_999, "EUR", ProductStatus.ACTIVE);
    Product expected = existing.withUpdatedAttributes(
        "NEW-SKU", "New Name", "New Desc", 5_999, "EUR", ProductStatus.ACTIVE, now);
    when(repository.save(any(Product.class))).thenReturn(expected);

    Product updated = service.updateProduct(command);

    assertEquals(expected, updated);
  }

  @Test
  void updateProductThrowsWhenNotFound() {
    UpdateProductCommand command = new UpdateProductCommand(
        ID, "NEW-SKU", "New Name", null, 5_999, "EUR", ProductStatus.ACTIVE);
    when(repository.findById(ID)).thenReturn(Optional.empty());

    assertThrows(ProductNotFoundException.class, () -> service.updateProduct(command));
  }

  @Test
  void updateProductRejectsConflictingSku() {
    Product existing = Product.create(ID, SKU, NAME, DESC, PRICE, CURRENCY, STATUS, now);
    when(repository.findById(ID)).thenReturn(Optional.of(existing));
    when(repository.existsBySkuAndIdNot("CONFLICT-SKU", ID)).thenReturn(true);

    UpdateProductCommand command = new UpdateProductCommand(
        ID, "CONFLICT-SKU", "New Name", null, 5_999, "EUR", ProductStatus.ACTIVE);

    assertThrows(DuplicateSkuException.class, () -> service.updateProduct(command));
  }

  @Test
  void deleteProductDeletesExisting() {
    when(repository.existsById(ID)).thenReturn(true);

    service.deleteProduct(ID);

    verify(repository).deleteById(ID);
  }

  @Test
  void deleteProductThrowsWhenNotFound() {
    when(repository.existsById(ID)).thenReturn(false);

    assertThrows(ProductNotFoundException.class, () -> service.deleteProduct(ID));
  }
}
