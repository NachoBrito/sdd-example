package es.nachobrito.sdd.example.adapter.messaging;

import es.nachobrito.sdd.example.domain.model.OrderLine;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Objects;
import java.util.UUID;

/**
 * Data transfer record for an order line item in order events.
 *
 * @param productId the identifier of the product
 * @param quantity the ordered quantity
 * @param unitPriceInCents the price per unit in integer cents
 * @param lineTotalInCents the total line amount in integer cents
 */
@Serdeable
public record OrderLinePayloadDto(
    UUID productId,
    int quantity,
    int unitPriceInCents,
    long lineTotalInCents) {

  /**
   * Validates required line item fields.
   */
  public OrderLinePayloadDto {
    Objects.requireNonNull(productId, "ProductId must not be null");
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be greater than zero");
    }
    if (unitPriceInCents < 0) {
      throw new IllegalArgumentException("UnitPriceInCents must be non-negative");
    }
    if (lineTotalInCents < 0) {
      throw new IllegalArgumentException("LineTotalInCents must be non-negative");
    }
  }

  /**
   * Creates an order line payload DTO from a domain order line.
   *
   * @param line the domain order line
   * @return the order line payload DTO
   */
  public static OrderLinePayloadDto fromDomain(OrderLine line) {
    Objects.requireNonNull(line, "OrderLine must not be null");
    return new OrderLinePayloadDto(
        line.productId(),
        line.quantity(),
        line.unitPriceInCents(),
        line.lineTotalInCents());
  }

  /**
   * Converts this payload DTO to a domain order line.
   *
   * @return the domain order line
   */
  public OrderLine toDomain() {
    return new OrderLine(productId, quantity, unitPriceInCents, lineTotalInCents);
  }
}

