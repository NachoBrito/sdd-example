package es.nachobrito.sdd.example.adapter.out.persistence;

import es.nachobrito.sdd.example.application.port.out.CategoryRepositoryPort;
import es.nachobrito.sdd.example.domain.model.Category;
import io.micronaut.data.model.Sort;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Persistence adapter implementing CategoryRepositoryPort with Micronaut Data JDBC.
 */
@Singleton
@DbQueryMetrics
public class CategoryPersistenceAdapter implements CategoryRepositoryPort {

  private final CategoryJdbcRepository repository;

  /**
   * Constructs the persistence adapter with the JDBC repository.
   *
   * @param repository the category JDBC repository
   */
  public CategoryPersistenceAdapter(CategoryJdbcRepository repository) {
    this.repository = repository;
  }

  @Override
  public Category save(Category category) {
    Objects.requireNonNull(category, "Category cannot be null");
    CategoryEntity entity = CategoryEntity.fromDomain(category);
    return repository.insert(entity).toDomain();
  }

  @Override
  public Optional<Category> findById(UUID id) {
    Objects.requireNonNull(id, "Category id cannot be null");
    return repository.findById(id).map(CategoryEntity::toDomain);
  }

  @Override
  public boolean existsById(UUID id) {
    Objects.requireNonNull(id, "Category id cannot be null");
    return repository.existsById(id);
  }

  @Override
  public void deleteById(UUID id) {
    Objects.requireNonNull(id, "Category id cannot be null");
    repository.deleteById(id);
  }

  @Override
  public List<Category> findAll(@Nullable String name, @Nullable String description) {
    String normalizedName = normalizeFilter(name);
    String normalizedDescription = normalizeFilter(description);
    return repository
        .search(normalizedName, normalizedDescription, Sort.of(Sort.Order.asc("name")))
        .stream()
        .map(CategoryEntity::toDomain)
        .toList();
  }

  private static @Nullable String normalizeFilter(@Nullable String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}

