package es.nachobrito.sdd.example.domain.model;

import java.util.UUID;

/**
 * Value object representing an order line item.
 *
 * @param productId the identifier of the product
 * @param quantity the ordered quantity
 * @param unitPriceInCents the unit price in integer cents
 * @param lineTotalInCents the calculated line total in integer cents
 */
public record OrderLine(
    UUID productId,
    int quantity,
    int unitPriceInCents,
    long lineTotalInCents) {

  /**
   * Compact constructor that validates order line invariants and enforces line totals.
   */
  public OrderLine {
    if (productId == null) {
      throw new InvalidOrderDataException("Order line productId cannot be null");
    }
    if (quantity <= 0) {
      throw new InvalidOrderDataException("Order line quantity must be greater than zero");
    }
    if (unitPriceInCents < 0) {
      throw new InvalidOrderDataException(
          "Order line unitPriceInCents must be non-negative: " + unitPriceInCents);
    }
    long expectedTotal = (long) quantity * unitPriceInCents;
    if (lineTotalInCents != expectedTotal) {
      throw new InvalidOrderDataException(
          "Order line total must equal quantity times unit price");
    }
  }

  /**
   * Convenience constructor calculating the line total from quantity and unit price.
   *
   * @param productId the product identifier
   * @param quantity the product quantity
   * @param unitPriceInCents the price per unit in cents
   */
  public OrderLine(UUID productId, int quantity, int unitPriceInCents) {
    this(productId, quantity, unitPriceInCents, (long) quantity * unitPriceInCents);
  }
}
