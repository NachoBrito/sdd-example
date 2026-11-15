package es.nachobrito.sdd.example.application.port.out;

import es.nachobrito.sdd.example.domain.model.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Outbound port defining category persistence operations.
 */
public interface CategoryRepositoryPort {

  /**
   * Saves a category to persistence.
   *
   * @param category the category to save
   * @return the persisted category
   */
  Category save(Category category);

  /**
   * Finds a category by its unique identifier.
   *
   * @param id the category identifier
   * @return an Optional with the category if found
   */
  Optional<Category> findById(UUID id);

  /**
   * Checks if a category exists with the given identifier.
   *
   * @param id the category identifier
   * @return true if the category exists
   */
  boolean existsById(UUID id);

  /**
   * Deletes a category by its unique identifier.
   *
   * @param id the category identifier
   */
  void deleteById(UUID id);

  /**
   * Finds all categories matching optional name and description filters.
   *
   * @param name optional case-insensitive name substring filter
   * @param description optional case-insensitive description substring filter
   * @return the matching categories ordered by name
   */
  List<Category> findAll(@Nullable String name, @Nullable String description);
}

