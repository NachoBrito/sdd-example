package es.nachobrito.sdd.example.domain.model;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Represents a product category entity.
 *
 * @param id the unique category identifier
 * @param name the category name
 * @param description the optional category description
 */
public record Category(UUID id, String name, @Nullable String description) {

  public static final int MAX_NAME_LENGTH = 255;
  public static final int MAX_DESCRIPTION_LENGTH = 4000;

  /**
   * Validates all category invariants.
   */
  public Category {
    if (id == null) {
      throw new InvalidCategoryDataException("Category id cannot be null");
    }
    if (name == null || name.isBlank()) {
      throw new InvalidCategoryDataException("Category name cannot be blank");
    }
    name = name.trim();
    if (name.length() > MAX_NAME_LENGTH) {
      throw new InvalidCategoryDataException(
          "Category name cannot exceed " + MAX_NAME_LENGTH + " characters");
    }
    if (description != null) {
      description = description.trim();
      if (description.length() > MAX_DESCRIPTION_LENGTH) {
        throw new InvalidCategoryDataException(
            "Category description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters");
      }
    }
  }
}

