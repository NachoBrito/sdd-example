package es.nachobrito.sdd.example.adapter.messaging;

import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderLine;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Data transfer record for complete order details in order events.
 *
 * @param id the unique order identifier
 * @param userId the owning user identifier
 * @param customerName the name of the customer
 * @param customerEmail the email address of the customer
 * @param date the order placement timestamp
 * @param status the order status name
 * @param totalAmountInCents the total monetary amount in integer cents
 * @param currency the three-letter ISO-4217 currency code
 * @param deliveryAddress the delivery address payload
 * @param invoiceAddress the invoice address payload
 * @param orderLines the list of order line item payloads
 */
@Serdeable
public record OrderPayloadDto(
    UUID id,
    @Nullable UUID userId,
    String customerName,
    String customerEmail,
    Instant date,
    String status,
    long totalAmountInCents,
    String currency,
    AddressPayloadDto deliveryAddress,
    AddressPayloadDto invoiceAddress,
    List<OrderLinePayloadDto> orderLines) {

  private static final UUID LEGACY_USER_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000001");

  /**
   * Validates required order payload fields.
   */
  public OrderPayloadDto {
    Objects.requireNonNull(id, "Id must not be null");
    if (userId == null) {
      userId = LEGACY_USER_ID;
    }
    Objects.requireNonNull(customerName, "CustomerName must not be null");
    Objects.requireNonNull(customerEmail, "CustomerEmail must not be null");
    Objects.requireNonNull(date, "Date must not be null");
    Objects.requireNonNull(status, "Status must not be null");
    Objects.requireNonNull(currency, "Currency must not be null");
    Objects.requireNonNull(deliveryAddress, "DeliveryAddress must not be null");
    Objects.requireNonNull(invoiceAddress, "InvoiceAddress must not be null");
    Objects.requireNonNull(orderLines, "OrderLines must not be null");
    if (orderLines.isEmpty()) {
      throw new IllegalArgumentException("OrderLines must not be empty");
    }
    if (totalAmountInCents < 0) {
      throw new IllegalArgumentException("TotalAmountInCents must be non-negative");
    }
    orderLines = List.copyOf(orderLines);
  }

  @Override
  public List<OrderLinePayloadDto> orderLines() {
    return Collections.unmodifiableList(orderLines);
  }

  /**
   * Creates an order payload DTO from a domain order.
   *
   * @param order the domain order
   * @return the order payload DTO
   */
  public static OrderPayloadDto fromDomain(Order order) {
    Objects.requireNonNull(order, "Order must not be null");
    List<OrderLinePayloadDto> lines =
        order.orderLines().stream().map(OrderLinePayloadDto::fromDomain).toList();
    return new OrderPayloadDto(
        order.id(),
        order.userId(),
        order.customerName(),
        order.customerEmail(),
        order.date(),
        order.status().name(),
        order.totalAmountInCents(),
        order.currency(),
        AddressPayloadDto.fromDeliveryAddress(order.deliveryAddress()),
        AddressPayloadDto.fromInvoiceAddress(order.invoiceAddress()),
        lines);
  }

  /**
   * Converts this payload DTO to a domain order.
   *
   * @param recordTimestamp the record creation and update timestamp
   * @return the domain order
   */
  public Order toDomain(Instant recordTimestamp) {
    Instant stamp = recordTimestamp != null ? recordTimestamp : date;
    List<OrderLine> lines =
        orderLines.stream().map(OrderLinePayloadDto::toDomain).toList();
    return new Order(
        id,
        userId != null ? userId : LEGACY_USER_ID,
        customerName,
        customerEmail,
        date,
        OrderStatus.valueOf(status),
        totalAmountInCents,
        currency,
        deliveryAddress.toDeliveryAddress(),
        invoiceAddress.toInvoiceAddress(),
        lines,
        stamp,
        stamp);
  }
}
