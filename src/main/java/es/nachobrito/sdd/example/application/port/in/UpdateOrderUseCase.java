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

/**
 * Inbound port for updating an existing order aggregate.
 */
@FunctionalInterface
public interface UpdateOrderUseCase {

  /**
   * Updates an existing order with new attribute values.
   *
   * @param command the update attributes
   * @return the updated order aggregate
   */
  Order updateOrder(UpdateOrderCommand command);

  /**
   * Command model containing attributes to update on an order.
   *
   * @param id the order identifier to update
   * @param customerName the updated customer name
   * @param customerEmail the updated customer email
   * @param date the updated order placement timestamp
   * @param status the updated order status
   * @param currency the updated currency code
   * @param deliveryAddress the updated delivery address
   * @param invoiceAddress the updated invoice address
   * @param orderLines the updated order lines
   */
  record UpdateOrderCommand(
      UUID id,
      String customerName,
      String customerEmail,
      Instant date,
      OrderStatus status,
      String currency,
      DeliveryAddress deliveryAddress,
      InvoiceAddress invoiceAddress,
      List<OrderLine> orderLines) {

    /**
     * Compact constructor that defensively copies order lines.
     */
    public UpdateOrderCommand {
      orderLines = orderLines == null ? List.of() : List.copyOf(orderLines);
    }

    @Override
    public List<OrderLine> orderLines() {
      return Collections.unmodifiableList(orderLines);
    }
  }
}

