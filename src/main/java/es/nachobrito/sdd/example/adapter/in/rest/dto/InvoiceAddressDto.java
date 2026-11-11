package es.nachobrito.sdd.example.adapter.in.rest.dto;

import es.nachobrito.sdd.example.domain.model.InvoiceAddress;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * REST DTO representing an invoice address.
 *
 * @param name the invoiced entity or person name
 * @param vatNumber the VAT or tax identifier
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
public record InvoiceAddressDto(
    @NotBlank @Size(max = 255) String name,
    @NotBlank @Size(max = 50) String vatNumber,
    @NotBlank @Size(max = 50) String telephone,
    @NotBlank @Size(max = 255) String street,
    @Nullable @Size(max = 255) String complement,
    @NotBlank @Size(max = 100) String city,
    @NotBlank @Size(max = 100) String state,
    @NotBlank @Size(max = 20) String zipCode,
    @NotBlank @Size(max = 100) String country) {

  /**
   * Converts this DTO to a domain InvoiceAddress value object.
   *
   * @return the domain value object
   */
  public InvoiceAddress toDomain() {
    return new InvoiceAddress(
        this.name,
        this.vatNumber,
        this.telephone,
        this.street,
        this.complement,
        this.city,
        this.state,
        this.zipCode,
        this.country);
  }

  /**
   * Creates an InvoiceAddressDto from a domain InvoiceAddress value object.
   *
   * @param domain the domain value object
   * @return the DTO
   */
  public static InvoiceAddressDto fromDomain(InvoiceAddress domain) {
    Objects.requireNonNull(domain, "InvoiceAddress cannot be null");
    return new InvoiceAddressDto(
        domain.name(),
        domain.vatNumber(),
        domain.telephone(),
        domain.street(),
        domain.complement(),
        domain.city(),
        domain.state(),
        domain.zipCode(),
        domain.country());
  }
}

