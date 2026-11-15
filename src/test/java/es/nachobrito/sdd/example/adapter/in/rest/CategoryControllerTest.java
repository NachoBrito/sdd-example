package es.nachobrito.sdd.example.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.adapter.in.rest.dto.CategoryResponse;
import es.nachobrito.sdd.example.adapter.in.rest.dto.CreateCategoryRequest;
import es.nachobrito.sdd.example.application.port.in.CategoryUseCase;
import es.nachobrito.sdd.example.application.port.in.CreateCategoryUseCase.CreateCategoryCommand;
import es.nachobrito.sdd.example.application.port.in.ListCategoriesUseCase.ListCategoriesQuery;
import es.nachobrito.sdd.example.domain.model.Category;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

  private static final UUID ID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

  @Mock
  private CategoryUseCase categoryUseCase;

  private CategoryController controller;

  @BeforeEach
  void setUp() {
    controller = new CategoryController(categoryUseCase);
  }

  @Test
  void createReturnsCreatedWithLocationAndBody() {
    CreateCategoryRequest request = new CreateCategoryRequest(ID, "Electronics", "Gadgets");
    Category category = new Category(ID, "Electronics", "Gadgets");
    when(categoryUseCase.createCategory(any(CreateCategoryCommand.class))).thenReturn(category);

    HttpResponse<CategoryResponse> response = controller.create(request);

    assertEquals(HttpStatus.CREATED, response.getStatus());
    assertEquals("/v1/categories/" + ID, response.getHeaders().get("Location"));
    assertEquals("Electronics", response.body().name());
    assertThrows(NullPointerException.class, () -> CategoryResponse.fromDomain(null));
  }

  @Test
  void listPassesFiltersAndMapsResponse() {
    Category category = new Category(ID, "Electronics", "Audio devices");
    when(categoryUseCase.listCategories(new ListCategoriesQuery("elec", "audio")))
        .thenReturn(List.of(category));

    List<CategoryResponse> result = controller.list("elec", "audio");

    assertEquals(1, result.size());
    assertEquals(ID, result.getFirst().id());
  }

  @Test
  void deleteDelegatesAndReturnsNoContent() {
    HttpResponse<Void> response = controller.delete(ID);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
    verify(categoryUseCase).deleteCategory(ID);
  }
}

