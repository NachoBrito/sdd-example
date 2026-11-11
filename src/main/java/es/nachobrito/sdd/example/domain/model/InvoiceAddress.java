package es.nachobrito.sdd.example.domain.model;

import org.jspecify.annotations.Nullable;

/**
 * Value object representing an invoice address.
 *
 * @param name the invoiced entity or person name
 * @param vatNumber the VAT or tax identification number
 * @param telephone the contact telephone number
 * @param street the street address
 * @param complement the optional address complement
 * @param city the city name
 * @param state the state or province name
 * @param zipCode the postal code
 * @param country the country name
 */
public record InvoiceAddress(
    String name,
    String vatNumber,
    String telephone,
    String street,
    @Nullable String complement,
    String city,
    String state,
    String zipCode,
    String country) {

  private static final int MAX_NAME_LENGTH = 255;
  private static final int MAX_VAT_NUMBER_LENGTH = 50;
  private static final int MAX_TELEPHONE_LENGTH = 50;
  private static final int MAX_STREET_LENGTH = 255;
  private static final int MAX_COMPLEMENT_LENGTH = 255;
  private static final int MAX_CITY_LENGTH = 100;
  private static final int MAX_STATE_LENGTH = 100;
  private static final int MAX_ZIP_CODE_LENGTH = 20;
  private static final int MAX_COUNTRY_LENGTH = 100;

  /**
   * Compact constructor that validates invoice address invariants.
   */
  public InvoiceAddress {
    name = validateRequired(name, "name", MAX_NAME_LENGTH);
    vatNumber = validateRequired(vatNumber, "vatNumber", MAX_VAT_NUMBER_LENGTH);
    telephone = validateRequired(telephone, "telephone", MAX_TELEPHONE_LENGTH);
    street = validateRequired(street, "street", MAX_STREET_LENGTH);
    complement = validateOptional(complement, "complement", MAX_COMPLEMENT_LENGTH);
    city = validateRequired(city, "city", MAX_CITY_LENGTH);
    state = validateRequired(state, "state", MAX_STATE_LENGTH);
    zipCode = validateRequired(zipCode, "zipCode", MAX_ZIP_CODE_LENGTH);
    country = validateRequired(country, "country", MAX_COUNTRY_LENGTH);
  }

  private static String validateRequired(String value, String fieldName, int maxLength) {
    if (value == null || value.isBlank()) {
      throw new InvalidOrderDataException("Invoice address " + fieldName + " cannot be blank");
    }
    String trimmed = value.trim();
    if (trimmed.length() > maxLength) {
      throw new InvalidOrderDataException(
          "Invoice address " + fieldName + " cannot exceed " + maxLength + " characters");
    }
    return trimmed;
  }

  private static @Nullable String validateOptional(
      @Nullable String value, String fieldName, int maxLength) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    if (trimmed.length() > maxLength) {
      throw new InvalidOrderDataException(
          "Invoice address " + fieldName + " cannot exceed " + maxLength + " characters");
    }
    return trimmed;
  }
}

