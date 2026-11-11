package es.nachobrito.sdd.example.adapter.out.persistence;

import es.nachobrito.sdd.example.domain.model.OrderLine;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import java.util.UUID;

/**
 * Persistence entity mapping the order_lines table.
 *
 * @param id the order line unique identifier
 * @param orderId the parent order identifier
 * @param productId the ordered product identifier
 * @param quantity the ordered quantity
 * @param unitPriceInCents the unit price in cents
 * @param lineTotalInCents the line total in cents
 */
@MappedEntity("order_lines")
public record OrderLineEntity(
    @Id @NonNull UUID id,
    @NonNull @MappedProperty("order_id") UUID orderId,
    @NonNull @MappedProperty("product_id") UUID productId,
    int quantity,
    @MappedProperty("unit_price_in_cents") int unitPriceInCents,
    @MappedProperty("line_total_in_cents") long lineTotalInCents) {

  /**
   * Converts this persistence entity to a domain OrderLine.
   *
   * @return the domain OrderLine value object
   */
  public OrderLine toDomain() {
    return new OrderLine(
        this.productId, this.quantity, this.unitPriceInCents, this.lineTotalInCents);
  }

  /**
   * Creates an OrderLineEntity from an OrderLine and parent order identifier.
   *
   * @param lineId the unique identifier for the entity record
   * @param orderId the parent order identifier
   * @param domain the domain OrderLine
   * @return the persistence entity
   */
  public static OrderLineEntity fromDomain(UUID lineId, UUID orderId, OrderLine domain) {
    return new OrderLineEntity(
        lineId,
        orderId,
        domain.productId(),
        domain.quantity(),
        domain.unitPriceInCents(),
        domain.lineTotalInCents());
  }
}

