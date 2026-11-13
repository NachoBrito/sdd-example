package es.nachobrito.sdd.example.application.port.in;

/**
 * Combined inbound use case port for authentication and token lifecycle operations.
 */
public interface AuthUseCase
    extends RegisterUserUseCase,
        LoginUseCase,
        ValidateTokenUseCase,
        RefreshTokenUseCase,
        RevokeTokenUseCase {}

