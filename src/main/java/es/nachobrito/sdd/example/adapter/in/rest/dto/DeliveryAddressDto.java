package es.nachobrito.sdd.example.adapter.in.rest.dto;

import es.nachobrito.sdd.example.domain.model.DeliveryAddress;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * REST DTO representing a delivery address.
 *
 * @param name the recipient name
 * @param telephone the contact telephone
 * @param street the street address
 * @param complement the optional complement
 * @param city the city name
 * @param state the state or province
 * @param zipCode the postal code
 * @param country the country name
 */
@Serdeable
@Introspected
public record DeliveryAddressDto(
    @NotBlank @Size(max = 255) String name,
    @NotBlank @Size(max = 50) String telephone,
    @NotBlank @Size(max = 255) String street,
    @Nullable @Size(max = 255) String complement,
    @NotBlank @Size(max = 100) String city,
    @NotBlank @Size(max = 100) String state,
    @NotBlank @Size(max = 20) String zipCode,
    @NotBlank @Size(max = 100) String country) {

  /**
   * Converts this DTO to a domain DeliveryAddress value object.
   *
   * @return the domain value object
   */
  public DeliveryAddress toDomain() {
    return new DeliveryAddress(
        this.name,
        this.telephone,
        this.street,
        this.complement,
        this.city,
        this.state,
        this.zipCode,
        this.country);
  }

  /**
   * Creates a DeliveryAddressDto from a domain DeliveryAddress value object.
   *
   * @param domain the domain value object
   * @return the DTO
   */
  public static DeliveryAddressDto fromDomain(DeliveryAddress domain) {
    Objects.requireNonNull(domain, "DeliveryAddress cannot be null");
    return new DeliveryAddressDto(
        domain.name(),
        domain.telephone(),
        domain.street(),
        domain.complement(),
        domain.city(),
        domain.state(),
        domain.zipCode(),
        domain.country());
  }
}

