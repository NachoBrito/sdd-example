package es.nachobrito.sdd.example.domain.model;

import java.util.UUID;

/**
 * Signals that a requested category does not exist.
 */
public class CategoryNotFoundException extends CategoryException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates the exception for the missing category identifier.
   *
   * @param id the missing category identifier
   */
  public CategoryNotFoundException(UUID id) {
    super("Category not found with identifier: " + id);
  }
}

