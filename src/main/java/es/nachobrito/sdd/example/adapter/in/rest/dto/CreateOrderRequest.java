package es.nachobrito.sdd.example.adapter.in.rest.dto;

import es.nachobrito.sdd.example.application.port.in.CreateOrderUseCase.CreateOrderCommand;
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
import org.jspecify.annotations.Nullable;

/**
 * Request payload for creating an order with a client-supplied identifier.
 *
 * @param id the unique order identifier
 * @param customerName the customer name
 * @param customerEmail the customer email address
 * @param date the optional order timestamp
 * @param status the optional initial status
 * @param totalAmountInCents the optional client-supplied total amount
 * @param currency the three-letter ISO-4217 currency code
 * @param deliveryAddress the delivery address DTO
 * @param invoiceAddress the invoice address DTO
 * @param orderLines the list of ordered lines
 */
@Serdeable
@Introspected
public record CreateOrderRequest(
    @NotNull UUID id,
    @NotBlank @Size(max = 255) String customerName,
    @NotBlank @Email @Size(max = 255) String customerEmail,
    @Nullable Instant date,
    @Nullable OrderStatus status,
    @Nullable Long totalAmountInCents,
    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO-4217 code")
    String currency,
    @NotNull @Valid DeliveryAddressDto deliveryAddress,
    @NotNull @Valid InvoiceAddressDto invoiceAddress,
    @NotEmpty List<@Valid OrderLineRequest> orderLines) {

  /**
   * Compact constructor that defensively copies order lines.
   */
  public CreateOrderRequest {
    orderLines = orderLines == null ? List.of() : List.copyOf(orderLines);
  }

  @Override
  public List<OrderLineRequest> orderLines() {
    return Collections.unmodifiableList(orderLines);
  }

  /**
   * Converts this request to a CreateOrderCommand for the given owning user.
   *
   * @param userId the owning user identifier
   * @return the command instance
   */
  public CreateOrderCommand toCommand(UUID userId) {
    return new CreateOrderCommand(
        this.id,
        userId,
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

