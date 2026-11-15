package es.nachobrito.sdd.example.domain.model;

import java.util.UUID;

/**
 * Signals that a category already uses the given identifier.
 */
public class DuplicateCategoryIdentifierException extends CategoryException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates the exception for the duplicate category identifier.
   *
   * @param id the duplicate category identifier
   */
  public DuplicateCategoryIdentifierException(UUID id) {
    super("Category already exists with identifier: " + id);
  }
}

