package es.nachobrito.sdd.example.adapter.in.rest.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for credential login.
 *
 * @param username the username
 * @param password the raw password
 */
@Serdeable
@Introspected
public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

