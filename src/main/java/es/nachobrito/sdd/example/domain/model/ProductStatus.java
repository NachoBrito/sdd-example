package es.nachobrito.sdd.example.domain.model;

/**
 * Represents the lifecycle status of a product.
 */
public enum ProductStatus {
  /** The product is in draft status and not yet active. */
  DRAFT,

  /** The product is active and visible in the catalog. */
  ACTIVE,

  /** The product is archived and discontinued. */
  ARCHIVED
}

