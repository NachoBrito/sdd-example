package es.nachobrito.sdd.example.adapter.out.persistence;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.Sort;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Micronaut Data JDBC repository for CategoryEntity.
 */
@JdbcRepository(dialect = Dialect.MYSQL)
public interface CategoryJdbcRepository extends GenericRepository<CategoryEntity, UUID> {

  /**
   * Finds a category entity by its unique identifier.
   *
   * @param id the category identifier
   * @return an Optional containing the entity if found
   */
  Optional<CategoryEntity> findById(UUID id);

  /**
   * Checks if a category entity exists with the specified identifier.
   *
   * @param id the category identifier
   * @return true if the entity exists
   */
  boolean existsById(UUID id);

  /**
   * Deletes a category entity by its identifier.
   *
   * @param id the category identifier
   */
  void deleteById(UUID id);

  /**
   * Inserts a new category entity with a client-assigned identifier.
   *
   * @param entity the entity to insert
   * @return the inserted entity
   */
  CategoryEntity insert(CategoryEntity entity);

  /**
   * Queries categories by optional name and description substrings.
   *
   * @param name optional case-insensitive name substring filter
   * @param description optional case-insensitive description substring filter
   * @param sort the sort order
   * @return the matching category entities
   */
  @Query(
      "SELECT * FROM categories category_entity_"
          + " WHERE (:name IS NULL OR :name = ''"
          + " OR LOCATE(LOWER(:name), LOWER(category_entity_.name)) > 0)"
          + " AND (:description IS NULL OR :description = ''"
          + " OR (category_entity_.description IS NOT NULL"
          + " AND LOCATE(LOWER(:description), LOWER(category_entity_.description)) > 0))")
  List<CategoryEntity> search(
      @Nullable String name,
      @Nullable String description,
      Sort sort);
}

