package es.nachobrito.sdd.example.adapter.in.rest.dto;

import es.nachobrito.sdd.example.domain.model.OrderLine;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * REST DTO representing an order line in a creation or update request.
 *
 * @param productId the unique product identifier
 * @param quantity the ordered quantity
 * @param unitPriceInCents the price per unit in integer cents
 * @param lineTotalInCents the optional client-supplied line total
 */
@Serdeable
@Introspected
public record OrderLineRequest(
    @NotNull UUID productId,
    @NotNull @Positive Integer quantity,
    @NotNull @PositiveOrZero Integer unitPriceInCents,
    @Nullable Long lineTotalInCents) {

  /**
   * Converts this request DTO to a domain OrderLine.
   *
   * @return the domain OrderLine value object
   */
  public OrderLine toDomain() {
    return new OrderLine(this.productId, this.quantity, this.unitPriceInCents);
  }
}

