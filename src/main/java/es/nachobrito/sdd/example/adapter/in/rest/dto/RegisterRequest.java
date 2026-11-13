package es.nachobrito.sdd.example.adapter.in.rest.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Request body for user registration.
 *
 * @param userId the client-generated user identifier
 * @param username the unique username
 * @param password the raw password
 */
@Serdeable
@Introspected
public record RegisterRequest(
    @NotNull UUID userId,
    @NotBlank @Size(min = 1, max = 100) String username,
    @NotBlank @Size(min = 8, max = 128) String password) {}

