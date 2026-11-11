package es.nachobrito.sdd.example.application.port.in;

import es.nachobrito.sdd.example.domain.model.Order;
import java.util.UUID;

/**
 * Inbound port for retrieving an order by its identifier.
 */
@FunctionalInterface
public interface GetOrderUseCase {

  /**
   * Retrieves an order aggregate by its unique identifier.
   *
   * @param id the order identifier
   * @return the found order aggregate
   */
  Order getOrder(UUID id);
}

