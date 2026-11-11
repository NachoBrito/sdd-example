package es.nachobrito.sdd.example.application.port.in;

import java.util.UUID;

/**
 * Inbound port for deleting an order aggregate by identifier.
 */
@FunctionalInterface
public interface DeleteOrderUseCase {

  /**
   * Deletes an order by its unique identifier.
   *
   * @param id the order identifier
   */
  void deleteOrder(UUID id);
}

