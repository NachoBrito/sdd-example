package es.nachobrito.sdd.example.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductStatus;
import io.micronaut.data.model.Sort;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductPersistenceAdapterQueryTest {

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
  void findByIdReturnsMappedProduct() {
    Product product = Product.create(
        ID, "SKU-P", "Product P", "Desc", 1_000, "USD", ProductStatus.ACTIVE, NOW);
    ProductEntity entity = ProductEntity.fromDomain(product);
    when(repository.findById(ID)).thenReturn(Optional.of(entity));

    Optional<Product> found = adapter.findById(ID);

    assertEquals(Optional.of(product), found);
  }

  @Test
  void findBySkuReturnsMappedProduct() {
    Product product = Product.create(
        ID, "SKU-P", "Product P", "Desc", 1_000, "USD", ProductStatus.ACTIVE, NOW);
    ProductEntity entity = ProductEntity.fromDomain(product);
    when(repository.findBySku("SKU-P")).thenReturn(Optional.of(entity));

    Optional<Product> found = adapter.findBySku("SKU-P");

    assertEquals(Optional.of(product), found);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void existsByIdDelegatesToRepository(boolean exists) {
    when(repository.existsById(ID)).thenReturn(exists);

    assertEquals(exists, adapter.existsById(ID));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void existsBySkuDelegatesToRepository(boolean exists) {
    when(repository.existsBySku("SKU-P")).thenReturn(exists);

    assertEquals(exists, adapter.existsBySku("SKU-P"));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void existsBySkuAndIdNotDelegatesToRepository(boolean exists) {
    when(repository.existsBySkuAndIdNot("SKU-P", ID)).thenReturn(exists);

    assertEquals(exists, adapter.existsBySkuAndIdNot("SKU-P", ID));
  }

  @Test
  void findAllExecutesQueryWithNullStatusAndAscSort() {
    Product product = Product.create(
        ID, "SKU-P", "Product P", "Desc", 1_000, "USD", ProductStatus.ACTIVE, NOW);
    ProductEntity entity = ProductEntity.fromDomain(product);
    when(repository.search(any(), any(), any(), any(Sort.class))).thenReturn(List.of(entity));

    List<Product> products = adapter.findAll("SKU-P", null, null, "status", "desc");

    assertEquals(List.of(product), products);
  }

  @Test
  void findAllExecutesQueryWithDefaultSortField() {
    Product product = Product.create(
        ID, "SKU-P", "Product P", "Desc", 1_000, "USD", ProductStatus.ACTIVE, NOW);
    ProductEntity entity = ProductEntity.fromDomain(product);
    when(repository.search(any(), any(), any(), any(Sort.class))).thenReturn(List.of(entity));

    List<Product> products = adapter.findAll(null, null, null, "createdat", "asc");

    assertEquals(List.of(product), products);
  }

  @Test
  void findAllExecutesQueryAndMapsResults() {
    Product product = Product.create(
        ID, "SKU-P", "Product P", "Desc", 1_000, "USD", ProductStatus.ACTIVE, NOW);
    ProductEntity entity = ProductEntity.fromDomain(product);
    when(repository.search(any(), any(), any(), any(Sort.class))).thenReturn(List.of(entity));

    List<Product> products = adapter.findAll(
        "SKU-P", null, ProductStatus.ACTIVE, "created_at", "desc");

    assertEquals(List.of(product), products);
  }
}
