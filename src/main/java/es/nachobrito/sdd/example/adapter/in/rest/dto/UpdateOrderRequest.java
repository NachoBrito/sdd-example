package es.nachobrito.sdd.example.adapter.in.rest.dto;

import es.nachobrito.sdd.example.application.port.in.UpdateOrderUseCase.UpdateOrderCommand;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Request payload for updating an order aggregate.
 *
 * @param customerName the updated customer name
 * @param customerEmail the updated customer email
 * @param date the updated order placement timestamp
 * @param status the updated order status
 * @param currency the updated currency code
 * @param deliveryAddress the updated delivery address DTO
 * @param invoiceAddress the updated invoice address DTO
 * @param orderLines the updated list of ordered lines
 */
@Serdeable
@Introspected
public record UpdateOrderRequest(
    @NotBlank @Size(max = 255) String customerName,
    @NotBlank @Email @Size(max = 255) String customerEmail,
    @NotNull Instant date,
    @NotNull OrderStatus status,
    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO-4217 code")
    String currency,
    @NotNull @Valid DeliveryAddressDto deliveryAddress,
    @NotNull @Valid InvoiceAddressDto invoiceAddress,
    @NotEmpty List<@Valid OrderLineRequest> orderLines) {

  /**
   * Compact constructor that defensively copies order lines.
   */
  public UpdateOrderRequest {
    orderLines = orderLines == null ? List.of() : List.copyOf(orderLines);
  }

  @Override
  public List<OrderLineRequest> orderLines() {
    return Collections.unmodifiableList(orderLines);
  }

  /**
   * Converts this request to an UpdateOrderCommand for the given identifier.
   *
   * @param id the order identifier
   * @return the command instance
   */
  public UpdateOrderCommand toCommand(UUID id) {
    return new UpdateOrderCommand(
        id,
        this.customerName,
        this.customerEmail,
        this.date,
        this.status,
        this.currency,
        this.deliveryAddress.toDomain(),
        this.invoiceAddress.toDomain(),
        this.orderLines.stream().map(OrderLineRequest::toDomain).toList());
  }
}

