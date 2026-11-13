package es.nachobrito.sdd.example.adapter.in.rest.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for refreshing an active JWT token pair.
 *
 * @param refreshToken the refresh JWT string
 */
@Serdeable
@Introspected
public record RefreshTokenRequest(@NotBlank String refreshToken) {}

