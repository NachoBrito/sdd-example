package es.nachobrito.sdd.example.domain.model;

import java.util.UUID;

/**
 * Thrown when an order cannot be deleted because of its current status.
 */
public class OrderDeletionConflictException extends OrderException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception for the non-deletable order.
   *
   * @param id the order identifier
   * @param status the current status of the order
   */
  public OrderDeletionConflictException(UUID id, OrderStatus status) {
    super("Cannot delete order " + id + " with status: " + status);
  }
}

