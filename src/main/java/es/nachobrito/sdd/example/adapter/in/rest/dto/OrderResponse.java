package es.nachobrito.sdd.example.adapter.in.rest.dto;

import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * REST API response model representing an order aggregate.
 *
 * @param id the order identifier
 * @param customerName the customer name
 * @param customerEmail the customer email
 * @param date the order placement timestamp
 * @param status the order lifecycle status
 * @param totalAmountInCents the total amount in cents
 * @param currency the three-letter ISO-4217 currency code
 * @param deliveryAddress the delivery address DTO
 * @param invoiceAddress the invoice address DTO
 * @param orderLines the list of order line DTOs
 * @param createdAt the creation timestamp
 * @param updatedAt the last update timestamp
 */
@Serdeable
@Introspected
public record OrderResponse(
    UUID id,
    String customerName,
    String customerEmail,
    Instant date,
    OrderStatus status,
    long totalAmountInCents,
    String currency,
    DeliveryAddressDto deliveryAddress,
    InvoiceAddressDto invoiceAddress,
    List<OrderLineResponse> orderLines,
    Instant createdAt,
    Instant updatedAt) {

  /**
   * Compact constructor that defensively copies the order lines list.
   */
  public OrderResponse {
    orderLines = orderLines == null ? List.of() : List.copyOf(orderLines);
  }

  @Override
  public List<OrderLineResponse> orderLines() {
    return Collections.unmodifiableList(orderLines);
  }

  /**
   * Maps a domain Order aggregate to an OrderResponse DTO.
   *
   * @param domain the domain Order aggregate
   * @return the response DTO
   */
  public static OrderResponse fromDomain(Order domain) {
    Objects.requireNonNull(domain, "Order cannot be null");
    List<OrderLineResponse> lineResponses = domain.orderLines().stream()
        .map(OrderLineResponse::fromDomain)
        .toList();

    return new OrderResponse(
        domain.id(),
        domain.customerName(),
        domain.customerEmail(),
        domain.date(),
        domain.status(),
        domain.totalAmountInCents(),
        domain.currency(),
        DeliveryAddressDto.fromDomain(domain.deliveryAddress()),
        InvoiceAddressDto.fromDomain(domain.invoiceAddress()),
        lineResponses,
        domain.createdAt(),
        domain.updatedAt());
  }
}

