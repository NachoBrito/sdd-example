package es.nachobrito.sdd.example.domain.model;

/**
 * Thrown when attempting to use a SKU that already exists in the catalog.
 */
public class DuplicateSkuException extends ProductException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception for the duplicated SKU.
   *
   * @param sku the conflicting stock keeping unit
   */
  public DuplicateSkuException(String sku) {
    super("Product already exists with SKU: " + sku);
  }
}

