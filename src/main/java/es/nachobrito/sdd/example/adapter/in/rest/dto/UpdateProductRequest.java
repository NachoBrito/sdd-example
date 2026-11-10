package es.nachobrito.sdd.example.adapter.in.rest.dto;

import es.nachobrito.sdd.example.domain.model.ProductStatus;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/**
 * Request payload for updating product attributes.
 *
 * @param sku the updated stock keeping unit
 * @param name the updated product name
 * @param description the updated optional description
 * @param priceInCents the updated price in integer cents
 * @param currency the updated ISO-4217 three-letter currency code
 * @param status the updated product status
 */
@Serdeable
@Introspected
public record UpdateProductRequest(
    @NotBlank @Size(max = 100) String sku,
    @NotBlank @Size(max = 255) String name,
    @Nullable @Size(max = 4000) String description,
    @NotNull @PositiveOrZero Integer priceInCents,
    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO-4217 code")
    String currency,
    @NotNull ProductStatus status) {}
