package es.nachobrito.sdd.example.domain.model;

import java.util.UUID;

/**
 * Thrown when an order does not exist with the specified identifier.
 */
public class OrderNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception for the missing order identifier.
   *
   * @param id the missing order identifier
   */
  public OrderNotFoundException(UUID id) {
    super("Order not found with identifier: " + id);
  }
}

