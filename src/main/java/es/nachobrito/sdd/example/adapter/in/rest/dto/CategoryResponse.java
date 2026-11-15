package es.nachobrito.sdd.example.adapter.in.rest.dto;

import es.nachobrito.sdd.example.domain.model.Category;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Objects;
import java.util.UUID;

/**
 * REST response representation of a category.
 *
 * @param id the category identifier
 * @param name the category name
 * @param description the optional category description
 */
@Serdeable
@Introspected
public record CategoryResponse(
    UUID id,
    String name,
    @Nullable String description) {

  /**
   * Maps a domain Category record into a CategoryResponse DTO.
   *
   * @param category the domain category
   * @return the REST response payload
   */
  public static CategoryResponse fromDomain(Category category) {
    Objects.requireNonNull(category, "Category cannot be null");
    return new CategoryResponse(category.id(), category.name(), category.description());
  }
}

