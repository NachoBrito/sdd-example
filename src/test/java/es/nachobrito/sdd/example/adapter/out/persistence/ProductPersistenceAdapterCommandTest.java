package es.nachobrito.sdd.example.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductPersistenceAdapterCommandTest {

  @Mock
  private ProductJdbcRepository repository;

  private ProductPersistenceAdapter adapter;

  private static final UUID ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");

  @BeforeEach
  void setUp() {
    adapter = new ProductPersistenceAdapter(repository);
  }

  @Test
  void saveInsertsWhenEntityDoesNotExist() {
    Product product = Product.create(
        ID, "SKU-P", "Product P", "Desc", 1_000, "USD", ProductStatus.ACTIVE, NOW);
    ProductEntity entity = ProductEntity.fromDomain(product);
    when(repository.existsById(ID)).thenReturn(false);
    when(repository.insert(any(ProductEntity.class))).thenReturn(entity);

    Product saved = adapter.save(product);

    assertEquals(product, saved);
  }

  @Test
  void saveUpdatesWhenEntityExists() {
    Product product = Product.create(
        ID, "SKU-P", "Product P", "Desc", 1_000, "USD", ProductStatus.ACTIVE, NOW);
    ProductEntity entity = ProductEntity.fromDomain(product);
    when(repository.existsById(ID)).thenReturn(true);
    when(repository.update(any(ProductEntity.class))).thenReturn(entity);

    Product saved = adapter.save(product);

    assertEquals(product, saved);
  }

  @Test
  void deleteByIdDelegatesToRepository() {
    adapter.deleteById(ID);

    verify(repository).deleteById(ID);
  }
}

