package es.nachobrito.sdd.example.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.application.port.in.CreateCategoryUseCase.CreateCategoryCommand;
import es.nachobrito.sdd.example.application.port.in.ListCategoriesUseCase.ListCategoriesQuery;
import es.nachobrito.sdd.example.application.port.out.CategoryRepositoryPort;
import es.nachobrito.sdd.example.application.port.out.ProductRepositoryPort;
import es.nachobrito.sdd.example.domain.model.Category;
import es.nachobrito.sdd.example.domain.model.CategoryInUseException;
import es.nachobrito.sdd.example.domain.model.CategoryNotFoundException;
import es.nachobrito.sdd.example.domain.model.DuplicateCategoryIdentifierException;
import es.nachobrito.sdd.example.domain.model.InvalidCategoryDataException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  private static final UUID ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

  @Mock
  private CategoryRepositoryPort categoryRepository;

  @Mock
  private ProductRepositoryPort productRepository;

  private CategoryService service;

  @BeforeEach
  void setUp() {
    service = new CategoryService(categoryRepository, productRepository);
  }

  @Test
  void createCategoryPersistsValidCategory() {
    CreateCategoryCommand command = new CreateCategoryCommand(ID, "Electronics", "Audio");
    Category expected = new Category(ID, "Electronics", "Audio");
    when(categoryRepository.existsById(ID)).thenReturn(false);
    when(categoryRepository.save(expected)).thenReturn(expected);

    Category created = service.createCategory(command);

    assertEquals(expected, created);
    verify(categoryRepository).save(expected);
  }

  @Test
  void createCategoryRejectsInvalidOrDuplicateInput() {
    assertThrows(NullPointerException.class, () -> service.createCategory(null));

    CreateCategoryCommand invalidCommand = new CreateCategoryCommand(null, "Books", null);
    assertThrows(
        InvalidCategoryDataException.class,
        () -> service.createCategory(invalidCommand));
    verify(categoryRepository, never()).existsById(any());

    CreateCategoryCommand duplicateCommand = new CreateCategoryCommand(ID, "Books", null);
    when(categoryRepository.existsById(ID)).thenReturn(true);
    assertThrows(
        DuplicateCategoryIdentifierException.class,
        () -> service.createCategory(duplicateCommand));
    verify(categoryRepository, never()).save(any());
  }

  @Test
  void listCategoriesDelegatesFilters() {
    Category category = new Category(ID, "Books", "Paperbacks");
    when(categoryRepository.findAll("book", "paper")).thenReturn(List.of(category));

    List<Category> result = service.listCategories(new ListCategoriesQuery("book", "paper"));

    assertEquals(1, result.size());
    assertEquals(ID, result.getFirst().id());
    assertThrows(NullPointerException.class, () -> service.listCategories(null));
  }

  @Test
  void deleteCategoryDeletesUnusedAndRejectsMissingOrInUse() {
    assertThrows(NullPointerException.class, () -> service.deleteCategory(null));

    when(categoryRepository.existsById(ID)).thenReturn(false);
    assertThrows(CategoryNotFoundException.class, () -> service.deleteCategory(ID));

    when(categoryRepository.existsById(ID)).thenReturn(true);
    when(productRepository.existsByCategoryId(ID)).thenReturn(true);
    assertThrows(CategoryInUseException.class, () -> service.deleteCategory(ID));
    verify(categoryRepository, never()).deleteById(ID);

    when(productRepository.existsByCategoryId(ID)).thenReturn(false);
    service.deleteCategory(ID);
    verify(categoryRepository).deleteById(ID);
  }
}

