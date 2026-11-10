package es.nachobrito.sdd.example.domain.model;

import java.util.UUID;

/**
 * Thrown when a product is not found by its identifier.
 */
public class ProductNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception for the specified identifier.
   *
   * @param id the missing product identifier
   */
  public ProductNotFoundException(UUID id) {
    super("Product not found with identifier: " + id);
  }
}

