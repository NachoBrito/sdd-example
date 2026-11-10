package es.nachobrito.sdd.example.application.port.in;

import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductStatus;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Inbound port for creating a product with client-generated identifier.
 */
@FunctionalInterface
public interface CreateProductUseCase {

  /**
   * Creates a new product according to the command attributes.
   *
   * @param command the product creation attributes
   * @return the created product
   */
  Product createProduct(CreateProductCommand command);

  /**
   * Command model containing attributes required to create a product.
   *
   * @param id the client-generated product identifier
   * @param sku the stock keeping unit
   * @param name the product name
   * @param description the optional description
   * @param priceInCents the product price in integer cents
   * @param currency the ISO-4217 currency code
   * @param status the product status
   */
  record CreateProductCommand(
      UUID id,
      String sku,
      String name,
      @Nullable String description,
      int priceInCents,
      String currency,
      ProductStatus status) {}
}

