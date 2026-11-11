package es.nachobrito.sdd.example.domain.model;

import java.util.UUID;

/**
 * Thrown when an order already exists with the specified identifier.
 */
public class DuplicateOrderIdentifierException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception for the duplicate order identifier.
   *
   * @param id the duplicate order identifier
   */
  public DuplicateOrderIdentifierException(UUID id) {
    super("Order already exists with identifier: " + id);
  }
}

