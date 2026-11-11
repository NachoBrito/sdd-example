package es.nachobrito.sdd.example.adapter.in.rest.dto;

import es.nachobrito.sdd.example.domain.model.OrderLine;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Objects;
import java.util.UUID;

/**
 * REST DTO representing an order line in an API response.
 *
 * @param productId the unique product identifier
 * @param quantity the ordered quantity
 * @param unitPriceInCents the unit price in integer cents
 * @param lineTotalInCents the calculated line total in integer cents
 */
@Serdeable
@Introspected
public record OrderLineResponse(
    UUID productId,
    int quantity,
    int unitPriceInCents,
    long lineTotalInCents) {

  /**
   * Maps a domain OrderLine to an OrderLineResponse DTO.
   *
   * @param domain the domain OrderLine
   * @return the response DTO
   */
  public static OrderLineResponse fromDomain(OrderLine domain) {
    Objects.requireNonNull(domain, "OrderLine cannot be null");
    return new OrderLineResponse(
        domain.productId(),
        domain.quantity(),
        domain.unitPriceInCents(),
        domain.lineTotalInCents());
  }
}

