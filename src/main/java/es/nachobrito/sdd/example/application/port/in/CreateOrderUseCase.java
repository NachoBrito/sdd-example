package es.nachobrito.sdd.example.application.port.in;

import es.nachobrito.sdd.example.domain.model.DeliveryAddress;
import es.nachobrito.sdd.example.domain.model.InvoiceAddress;
import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderLine;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Inbound port for creating an order with a client-generated identifier.
 */
@FunctionalInterface
public interface CreateOrderUseCase {

  /**
   * Creates a new order according to the command attributes.
   *
   * @param command the order creation attributes
   * @return the created order aggregate
   */
  Order createOrder(CreateOrderCommand command);

  /**
   * Command model containing attributes required to create an order.
   *
   * @param id the client-generated order identifier
   * @param customerName the customer name
   * @param customerEmail the customer email
   * @param date optional order placement timestamp
   * @param status optional initial status
   * @param currency the three-letter currency code
   * @param deliveryAddress the delivery address details
   * @param invoiceAddress the invoice address details
   * @param orderLines the list of ordered line items
   */
  record CreateOrderCommand(
      UUID id,
      String customerName,
      String customerEmail,
      @Nullable Instant date,
      @Nullable OrderStatus status,
      String currency,
      DeliveryAddress deliveryAddress,
      InvoiceAddress invoiceAddress,
      List<OrderLine> orderLines) {

    /**
     * Compact constructor that defensively copies order lines.
     */
    public CreateOrderCommand {
      orderLines = orderLines == null ? List.of() : List.copyOf(orderLines);
    }

    @Override
    public List<OrderLine> orderLines() {
      return Collections.unmodifiableList(orderLines);
    }
  }
}

