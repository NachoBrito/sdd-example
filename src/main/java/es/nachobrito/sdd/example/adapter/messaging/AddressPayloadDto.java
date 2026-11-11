package es.nachobrito.sdd.example.adapter.messaging;

import es.nachobrito.sdd.example.domain.model.DeliveryAddress;
import es.nachobrito.sdd.example.domain.model.InvoiceAddress;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Data transfer record for an address payload in order events.
 *
 * @param name the contact name
 * @param vatNumber the VAT number for invoice addresses
 * @param telephone the contact telephone
 * @param street the street address
 * @param complement the address complement line
 * @param city the city name
 * @param state the state or province name
 * @param zipCode the postal code
 * @param country the country name or code
 */
@Serdeable
public record AddressPayloadDto(
    String name,
    @Nullable String vatNumber,
    String telephone,
    String street,
    @Nullable String complement,
    String city,
    String state,
    String zipCode,
    String country) {

  /**
   * Validates required address fields.
   */
  public AddressPayloadDto {
    Objects.requireNonNull(name, "Name must not be null");
    Objects.requireNonNull(telephone, "Telephone must not be null");
    Objects.requireNonNull(street, "Street must not be null");
    Objects.requireNonNull(city, "City must not be null");
    Objects.requireNonNull(state, "State must not be null");
    Objects.requireNonNull(zipCode, "ZipCode must not be null");
    Objects.requireNonNull(country, "Country must not be null");
  }

  /**
   * Creates an address payload DTO without VAT number.
   *
   * @param name the contact name
   * @param telephone the telephone
   * @param street the street
   * @param complement the complement
   * @param city the city
   * @param state the state
   * @param zipCode the postal code
   * @param country the country
   */
  public AddressPayloadDto(
      String name,
      String telephone,
      String street,
      @Nullable String complement,
      String city,
      String state,
      String zipCode,
      String country) {
    this(name, null, telephone, street, complement, city, state, zipCode, country);
  }

  /**
   * Creates a payload DTO from a domain delivery address.
   *
   * @param address the delivery address
   * @return the address payload DTO
   */
  public static AddressPayloadDto fromDeliveryAddress(DeliveryAddress address) {
    Objects.requireNonNull(address, "Address must not be null");
    return new AddressPayloadDto(
        address.name(),
        null,
        address.telephone(),
        address.street(),
        address.complement(),
        address.city(),
        address.state(),
        address.zipCode(),
        address.country());
  }

  /**
   * Creates a payload DTO from a domain invoice address.
   *
   * @param address the invoice address
   * @return the address payload DTO
   */
  public static AddressPayloadDto fromInvoiceAddress(InvoiceAddress address) {
    Objects.requireNonNull(address, "Address must not be null");
    return new AddressPayloadDto(
        address.name(),
        address.vatNumber(),
        address.telephone(),
        address.street(),
        address.complement(),
        address.city(),
        address.state(),
        address.zipCode(),
        address.country());
  }

  /**
   * Converts this payload DTO to a domain delivery address.
   *
   * @return the delivery address
   */
  public DeliveryAddress toDeliveryAddress() {
    return new DeliveryAddress(name, telephone, street, complement, city, state, zipCode, country);
  }

  /**
   * Converts this payload DTO to a domain invoice address.
   *
   * @return the invoice address
   */
  public InvoiceAddress toInvoiceAddress() {
    String vat = vatNumber != null && !vatNumber.isBlank() ? vatNumber : "N/A";
    return new InvoiceAddress(
        name, vat, telephone, street, complement, city, state, zipCode, country);
  }
}
