package es.nachobrito.sdd.example.application.port.in;

import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductStatus;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Inbound port for updating an existing product.
 */
@FunctionalInterface
public interface UpdateProductUseCase {

  /**
   * Updates an existing product with new attribute values.
   *
   * @param command the update attributes
   * @return the updated product
   */
  Product updateProduct(UpdateProductCommand command);

  /**
   * Command model containing attributes to update on a product.
   *
   * @param id the product identifier to update
   * @param sku the updated stock keeping unit
   * @param name the updated product name
   * @param description the updated optional description
   * @param priceInCents the updated price in integer cents
   * @param currency the updated ISO-4217 currency code
   * @param status the updated product status
   */
  record UpdateProductCommand(
      UUID id,
      String sku,
      String name,
      @Nullable String description,
      int priceInCents,
      String currency,
      ProductStatus status) {}
}

