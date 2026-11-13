package es.nachobrito.sdd.example.adapter.in.rest.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import org.jspecify.annotations.Nullable;

/**
 * Optional request body for token validation or revocation.
 *
 * @param token the JWT token string, or null to use the Authorization header
 */
@Serdeable
@Introspected
public record TokenRequest(@Nullable String token) {}

