package es.nachobrito.sdd.example.adapter.in.rest.dto;

import es.nachobrito.sdd.example.domain.model.ActiveDiscount;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Objects;
import java.util.UUID;

/**
 * REST representation of an active promotional discount applied at request time.
 *
 * @param campaignId the campaign identifier
 * @param campaignName the campaign name
 * @param discountPercent the discount percentage
 */
@Serdeable
@Introspected
public record ProductDiscountResponse(
    UUID campaignId,
    String campaignName,
    int discountPercent) {

  /**
   * Maps a domain ActiveDiscount value object to a ProductDiscountResponse DTO.
   *
   * @param discount the active discount projection
   * @return the REST discount DTO
   */
  public static ProductDiscountResponse fromDomain(ActiveDiscount discount) {
    Objects.requireNonNull(discount, "Active discount cannot be null");
    return new ProductDiscountResponse(
        discount.campaignId(),
        discount.campaignName(),
        discount.discountPercent());
  }
}

