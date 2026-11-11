package es.nachobrito.sdd.example.domain.model;

import java.util.UUID;

/**
 * Thrown when attempting to create a product with an identifier that already exists.
 */
public class DuplicateIdentifierException extends ProductException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception for the duplicated identifier.
   *
   * @param id the conflicting identifier
   */
  public DuplicateIdentifierException(UUID id) {
    super("Product already exists with identifier: " + id);
  }
}

