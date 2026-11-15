package es.nachobrito.sdd.example.application.port.in;

import es.nachobrito.sdd.example.domain.model.Category;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Inbound port for listing and filtering product categories.
 */
@FunctionalInterface
public interface ListCategoriesUseCase {

  /**
   * Retrieves categories matching optional filters.
   *
   * @param query the category filter criteria
   * @return the list of matching categories
   */
  List<Category> listCategories(ListCategoriesQuery query);

  /**
   * Query model for filtering categories.
   *
   * @param name optional case-insensitive name substring filter
   * @param description optional case-insensitive description substring filter
   */
  record ListCategoriesQuery(@Nullable String name, @Nullable String description) {}
}

