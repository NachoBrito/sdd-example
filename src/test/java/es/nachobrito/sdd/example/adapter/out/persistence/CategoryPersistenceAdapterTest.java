package es.nachobrito.sdd.example.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.domain.model.Category;
import io.micronaut.data.model.Sort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryPersistenceAdapterTest {

  private static final UUID ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

  @Mock
  private CategoryJdbcRepository repository;

  private CategoryPersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new CategoryPersistenceAdapter(repository);
  }

  @Test
  void saveInsertsAndReturnsDomainCategory() {
    Category category = new Category(ID, "Electronics", "Gadgets");
    CategoryEntity entity = CategoryEntity.fromDomain(category);
    when(repository.insert(entity)).thenReturn(entity);

    Category saved = adapter.save(category);

    assertEquals(category, saved);
    verify(repository).insert(entity);
    assertThrows(NullPointerException.class, () -> adapter.save(null));
    assertThrows(NullPointerException.class, () -> CategoryEntity.fromDomain(null));
  }

  @Test
  void findAndExistenceAndDeleteDelegateCorrectly() {
    CategoryEntity entity = new CategoryEntity(ID, "Books", null);
    when(repository.findById(ID)).thenReturn(Optional.of(entity));
    when(repository.existsById(ID)).thenReturn(true);

    Optional<Category> found = adapter.findById(ID);
    assertTrue(found.isPresent());
    assertEquals("Books", found.get().name());
    assertTrue(adapter.existsById(ID));

    when(repository.existsById(ID)).thenReturn(false);
    assertFalse(adapter.existsById(ID));

    adapter.deleteById(ID);
    verify(repository).deleteById(ID);

    assertThrows(NullPointerException.class, () -> adapter.findById(null));
    assertThrows(NullPointerException.class, () -> adapter.existsById(null));
    assertThrows(NullPointerException.class, () -> adapter.deleteById(null));
  }

  @Test
  void findAllNormalizesBlankAndTrimmedFiltersAndSortsAscending() {
    Sort expectedSort = Sort.of(Sort.Order.asc("name"));
    CategoryEntity entity = new CategoryEntity(ID, "Electronics", "Home audio");
    when(repository.search("elec", "audio", expectedSort)).thenReturn(List.of(entity));
    when(repository.search(null, null, expectedSort)).thenReturn(List.of(entity));

    List<Category> filtered = adapter.findAll("  elec ", " audio ");
    assertEquals(1, filtered.size());
    assertEquals("Electronics", filtered.getFirst().name());

    List<Category> unfiltered = adapter.findAll("   ", null);
    assertEquals(1, unfiltered.size());
    verify(repository).search(null, null, expectedSort);
  }
}

