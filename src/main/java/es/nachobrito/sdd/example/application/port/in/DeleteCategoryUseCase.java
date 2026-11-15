package es.nachobrito.sdd.example.application.port.in;

import java.util.UUID;

/**
 * Inbound port for deleting a product category.
 */
@FunctionalInterface
public interface DeleteCategoryUseCase {

  /**
   * Deletes an unused category by identifier.
   *
   * @param id the category identifier
   */
  void deleteCategory(UUID id);
}

