package es.nachobrito.sdd.example.application.service;

import es.nachobrito.sdd.example.application.port.in.CategoryUseCase;
import es.nachobrito.sdd.example.application.port.out.CategoryRepositoryPort;
import es.nachobrito.sdd.example.application.port.out.ProductRepositoryPort;
import es.nachobrito.sdd.example.domain.model.Category;
import es.nachobrito.sdd.example.domain.model.CategoryInUseException;
import es.nachobrito.sdd.example.domain.model.CategoryNotFoundException;
import es.nachobrito.sdd.example.domain.model.DuplicateCategoryIdentifierException;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Application service implementing category management use cases.
 */
@Singleton
public class CategoryService implements CategoryUseCase {

  private final CategoryRepositoryPort categoryRepository;
  private final ProductRepositoryPort productRepository;

  /**
   * Constructs the service with category and product repositories.
   *
   * @param categoryRepository the category persistence port
   * @param productRepository the product persistence port
   */
  public CategoryService(
      CategoryRepositoryPort categoryRepository,
      ProductRepositoryPort productRepository) {
    this.categoryRepository = categoryRepository;
    this.productRepository = productRepository;
  }

  @Override
  @Transactional
  public Category createCategory(CreateCategoryCommand command) {
    Objects.requireNonNull(command, "Command cannot be null");
    Category category = new Category(command.id(), command.name(), command.description());
    if (categoryRepository.existsById(category.id())) {
      throw new DuplicateCategoryIdentifierException(category.id());
    }
    return categoryRepository.save(category);
  }

  @Override
  public List<Category> listCategories(ListCategoriesQuery query) {
    Objects.requireNonNull(query, "Query cannot be null");
    return categoryRepository.findAll(query.name(), query.description());
  }

  @Override
  @Transactional
  public void deleteCategory(UUID id) {
    Objects.requireNonNull(id, "Category id cannot be null");
    if (!categoryRepository.existsById(id)) {
      throw new CategoryNotFoundException(id);
    }
    if (productRepository.existsByCategoryId(id)) {
      throw new CategoryInUseException(id);
    }
    categoryRepository.deleteById(id);
  }
}

