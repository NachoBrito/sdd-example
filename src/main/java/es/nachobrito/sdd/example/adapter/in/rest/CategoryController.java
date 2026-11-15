package es.nachobrito.sdd.example.adapter.in.rest;

import es.nachobrito.sdd.example.adapter.in.rest.dto.CategoryResponse;
import es.nachobrito.sdd.example.adapter.in.rest.dto.CreateCategoryRequest;
import es.nachobrito.sdd.example.application.port.in.CategoryUseCase;
import es.nachobrito.sdd.example.application.port.in.CreateCategoryUseCase.CreateCategoryCommand;
import es.nachobrito.sdd.example.application.port.in.ListCategoriesUseCase.ListCategoriesQuery;
import es.nachobrito.sdd.example.domain.model.Category;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.annotation.Status;
import io.micronaut.security.annotation.Secured;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * REST controller for managing product category resources.
 */
@Validated
@Controller("/v1/categories")
@Tag(name = "Categories", description = "Endpoints for managing product categories")
public class CategoryController {

  private final CategoryUseCase categoryUseCase;

  /**
   * Constructs the controller with the category use cases.
   *
   * @param categoryUseCase the composite category use case port
   */
  public CategoryController(CategoryUseCase categoryUseCase) {
    this.categoryUseCase = categoryUseCase;
  }

  /**
   * Creates a category with a client-generated identifier.
   *
   * @param request the category creation request
   * @return 201 Created with the category and Location header
   */
  @Post
  @Secured({"ADMIN"})
  @Operation(
      summary = "Create category",
      description = "Creates a category with a client-generated UUID")
  @ApiResponse(responseCode = "201", description = "Category created successfully")
  @ApiResponse(responseCode = "400", description = "Validation error")
  @ApiResponse(responseCode = "403", description = "ADMIN role required")
  @ApiResponse(responseCode = "409", description = "Category identifier already exists")
  public HttpResponse<CategoryResponse> create(@Body @Valid CreateCategoryRequest request) {
    CreateCategoryCommand command =
        new CreateCategoryCommand(request.id(), request.name(), request.description());
    Category created = categoryUseCase.createCategory(command);
    return HttpResponse.created(
        CategoryResponse.fromDomain(created),
        URI.create("/v1/categories/" + created.id()));
  }

  /**
   * Lists categories matching optional name and description filters.
   *
   * @param name optional case-insensitive name substring filter
   * @param description optional case-insensitive description substring filter
   * @return the list of matching categories
   */
  @Get
  @Secured({"CUSTOMER", "CATALOG"})
  @Operation(
      summary = "List categories",
      description = "Lists categories with optional substring filters on name and description")
  @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
  @ApiResponse(responseCode = "403", description = "CUSTOMER or CATALOG role required")
  public List<CategoryResponse> list(
      @Nullable @QueryValue String name,
      @Nullable @QueryValue String description) {
    return categoryUseCase.listCategories(new ListCategoriesQuery(name, description))
        .stream()
        .map(CategoryResponse::fromDomain)
        .toList();
  }

  /**
   * Permanently deletes an unused category by identifier.
   *
   * @param id the category identifier
   * @return 204 No Content
   */
  @Delete("/{id}")
  @Status(HttpStatus.NO_CONTENT)
  @Secured({"ADMIN"})
  @Operation(
      summary = "Delete category",
      description = "Deletes an unused category by identifier")
  @ApiResponse(responseCode = "204", description = "Category deleted successfully")
  @ApiResponse(responseCode = "403", description = "ADMIN role required")
  @ApiResponse(responseCode = "404", description = "Category not found")
  @ApiResponse(responseCode = "409", description = "Category is assigned to products")
  public HttpResponse<Void> delete(@PathVariable UUID id) {
    categoryUseCase.deleteCategory(id);
    return HttpResponse.noContent();
  }
}

