package es.nachobrito.sdd.example.application.port.in;

import es.nachobrito.sdd.example.domain.model.Product;
import java.util.UUID;

/**
 * Inbound port for retrieving a product by identifier.
 */
@FunctionalInterface
public interface GetProductUseCase {

  /**
   * Retrieves a product by its unique identifier.
   *
   * @param id the product identifier
   * @return the found product
   */
  Product getProduct(UUID id);
}

