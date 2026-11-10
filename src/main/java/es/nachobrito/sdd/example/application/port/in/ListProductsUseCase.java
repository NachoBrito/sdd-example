package es.nachobrito.sdd.example.application.port.in;

import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductStatus;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Inbound port for searching, filtering, and sorting products.
 */
@FunctionalInterface
public interface ListProductsUseCase {

  /**
   * Retrieves products matching the filter query.
   *
   * @param query the search and sort query parameters
   * @return the list of matching products
   */
  List<Product> listProducts(ListProductsQuery query);

  /**
   * Query model for product search and filtering.
   *
   * @param sku optional SKU filter
   * @param nameRegex optional regular expression for name matching
   * @param status optional lifecycle status filter
   * @param sortField the sort field (name, status, createdAt)
   * @param direction the sort direction (asc, desc)
   */
  record ListProductsQuery(
      @Nullable String sku,
      @Nullable String nameRegex,
      @Nullable ProductStatus status,
      String sortField,
      String direction) {}
}

