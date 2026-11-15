package es.nachobrito.sdd.example.adapter.out.persistence;

import es.nachobrito.sdd.example.domain.model.Category;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import java.util.Objects;
import java.util.UUID;

/**
 * Persistence entity mapping the MariaDB categories table.
 *
 * @param id the primary key UUID
 * @param name the category name
 * @param description the optional category description
 */
@MappedEntity("categories")
public record CategoryEntity(
    @Id @NonNull UUID id,
    @NonNull String name,
    @Nullable String description) {

  /**
   * Converts this persistence entity to a Category domain record.
   *
   * @return the Category domain record
   */
  public Category toDomain() {
    return new Category(this.id, this.name, this.description);
  }

  /**
   * Creates a persistence entity from a Category domain record.
   *
   * @param domain the Category domain record
   * @return the corresponding CategoryEntity
   */
  public static CategoryEntity fromDomain(Category domain) {
    Objects.requireNonNull(domain, "Category cannot be null");
    return new CategoryEntity(domain.id(), domain.name(), domain.description());
  }
}

