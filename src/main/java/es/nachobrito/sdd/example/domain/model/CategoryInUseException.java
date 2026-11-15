package es.nachobrito.sdd.example.domain.model;

import java.util.UUID;

/**
 * Signals that a category has assigned products and cannot be deleted.
 */
public class CategoryInUseException extends CategoryException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates the exception for the category in use.
   *
   * @param id the identifier of the category in use
   */
  public CategoryInUseException(UUID id) {
    super("Category is assigned to one or more products: " + id);
  }
}

