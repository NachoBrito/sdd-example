package es.nachobrito.sdd.example.application.port.in;

import es.nachobrito.sdd.example.domain.model.Category;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Inbound port for creating a product category.
 */
@FunctionalInterface
public interface CreateCategoryUseCase {

  /**
   * Creates a category with a client-generated identifier.
   *
   * @param command the category creation attributes
   * @return the created category
   */
  Category createCategory(CreateCategoryCommand command);

  /**
   * Command model for creating a category.
   *
   * @param id the client-generated category identifier
   * @param name the category name
   * @param description the optional category description
   */
  record CreateCategoryCommand(UUID id, String name, @Nullable String description) {}
}

