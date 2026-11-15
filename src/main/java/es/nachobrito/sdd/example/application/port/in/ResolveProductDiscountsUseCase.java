package es.nachobrito.sdd.example.application.port.in;

import es.nachobrito.sdd.example.domain.model.ActiveDiscount;
import es.nachobrito.sdd.example.domain.model.Product;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Inbound port for resolving active promotional discounts at request time.
 */
public interface ResolveProductDiscountsUseCase {

  /**
   * Resolves all active campaign discounts applicable to a single product.
   *
   * @param product the target product
   * @return the list of matching active discounts
   */
  List<ActiveDiscount> resolveActiveDiscounts(Product product);

  /**
   * Resolves all active campaign discounts applicable to a list of products.
   *
   * @param products the target products
   * @return a map from product identifier to matching active discounts
   */
  Map<UUID, List<ActiveDiscount>> resolveActiveDiscounts(List<Product> products);
}

