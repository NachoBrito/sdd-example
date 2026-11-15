package es.nachobrito.sdd.example.adapter.in.rest.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * REST request payload for creating a category.
 *
 * @param id the client-generated category identifier
 * @param name the category name
 * @param description the optional category description
 */
@Serdeable
@Introspected
public record CreateCategoryRequest(
    @NotNull UUID id,
    @NotBlank @Size(max = 255) String name,
    @Nullable @Size(max = 4000) String description) {
}

