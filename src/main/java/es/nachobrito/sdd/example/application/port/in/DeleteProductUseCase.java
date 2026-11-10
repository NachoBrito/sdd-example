package es.nachobrito.sdd.example.application.port.in;

import java.util.UUID;

/**
 * Inbound port for permanently removing a product by identifier.
 */
@FunctionalInterface
public interface DeleteProductUseCase {

  /**
   * Permanently deletes a product by identifier.
   *
   * @param id the product identifier
   */
  void deleteProduct(UUID id);
}

