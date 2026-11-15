package es.nachobrito.sdd.example.adapter.in.rest;

import es.nachobrito.sdd.example.adapter.in.rest.dto.CreateProductV2Request;
import es.nachobrito.sdd.example.adapter.in.rest.dto.ProductResponse;
import es.nachobrito.sdd.example.adapter.in.rest.dto.UpdateProductV2Request;
import es.nachobrito.sdd.example.application.port.in.ProductUseCase;
import es.nachobrito.sdd.example.application.port.in.ResolveProductDiscountsUseCase;
import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductStatus;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Version 2 REST controller for managing products with a required category identifier.
 */
@Validated
@Controller("/v2/products")
@Tag(
    name = "Products v2",
    description = "Version 2 endpoints for managing products with a required category")
public class ProductV2Controller {

  private final ProductUseCase productUseCase;
  private final ResolveProductDiscountsUseCase resolveProductDiscountsUseCase;

  /**
   * Constructs the controller with product management and discount resolution use cases.
   *
   * @param productUseCase the composite product use case port
   * @param resolveProductDiscountsUseCase the request-time discount resolution port
   */
  public ProductV2Controller(
      ProductUseCase productUseCase,
      ResolveProductDiscountsUseCase resolveProductDiscountsUseCase) {
    this.productUseCase = productUseCase;
    this.resolveProductDiscountsUseCase = resolveProductDiscountsUseCase;
  }

  /**
   * Creates a new product with a client-generated identifier and required category.
   *
   * @param request the version 2 creation request body
   * @return 201 Created with the product and Location header
   */
  @Post
  @Secured({"CATALOG"})
  @Operation(
      summary = "Create product (v2)",
      description = "Creates a product with client-generated identifier and required categoryId")
  @ApiResponse(responseCode = "201", description = "Product created successfully")
  @ApiResponse(responseCode = "400", description = "Validation error or unknown category")
  @ApiResponse(responseCode = "409", description = "Identifier or SKU already exists")
  public HttpResponse<ProductResponse> create(@Body @Valid CreateProductV2Request request) {
    ProductUseCase.CreateProductCommand command = new ProductUseCase.CreateProductCommand(
        request.id(),
        request.sku(),
        request.name(),
        request.description(),
        request.priceInCents(),
        request.currency(),
        request.status(),
        request.categoryId());

    Product created = productUseCase.createProduct(command);
    URI location = URI.create("/v2/products/" + created.id());
    return HttpResponse.created(ProductResponse.fromDomain(created), location);
  }

  /**
   * Retrieves a product by identifier.
   *
   * @param id the product identifier
   * @return 200 OK with product details
   */
  @Get("/{id}")
  @Secured({"CUSTOMER", "CATALOG"})
  @Operation(summary = "Get product (v2)", description = "Retrieves a product by identifier")
  @ApiResponse(responseCode = "200", description = "Product found")
  @ApiResponse(responseCode = "404", description = "Product not found")
  public ProductResponse getById(@PathVariable UUID id) {
    Product product = productUseCase.getProduct(id);
    return ProductResponse.fromDomain(
        product, resolveProductDiscountsUseCase.resolveActiveDiscounts(product));
  }

  /**
   * Updates an existing product by identifier with a required category.
   *
   * @param id the product identifier
   * @param request the version 2 update request body
   * @return 200 OK with updated product
   */
  @Put("/{id}")
  @Secured({"CATALOG"})
  @Operation(
      summary = "Update product (v2)",
      description = "Updates an existing product with a required categoryId")
  @ApiResponse(responseCode = "200", description = "Product updated")
  @ApiResponse(responseCode = "400", description = "Validation error or unknown category")
  @ApiResponse(responseCode = "404", description = "Product not found")
  @ApiResponse(responseCode = "409", description = "SKU already exists")
  public ProductResponse update(
      @PathVariable UUID id,
      @Body @Valid UpdateProductV2Request request) {
    ProductUseCase.UpdateProductCommand command = new ProductUseCase.UpdateProductCommand(
        id,
        request.sku(),
        request.name(),
        request.description(),
        request.priceInCents(),
        request.currency(),
        request.status(),
        request.categoryId());

    Product updated = productUseCase.updateProduct(command);
    return ProductResponse.fromDomain(updated);
  }

  /**
   * Permanently deletes a product by identifier.
   *
   * @param id the product identifier
   * @return 204 No Content
   */
  @Delete("/{id}")
  @Secured({"CATALOG"})
  @Operation(
      summary = "Delete product (v2)",
      description = "Permanently deletes a product by identifier")
  @ApiResponse(responseCode = "204", description = "Product permanently deleted")
  @ApiResponse(responseCode = "404", description = "Product not found")
  public HttpResponse<Void> delete(@PathVariable UUID id) {
    productUseCase.deleteProduct(id);
    return HttpResponse.noContent();
  }

  /**
   * Lists products with optional filtering and sorting.
   *
   * @param sku optional SKU filter
   * @param nameRegex optional regular expression for name matching
   * @param status optional status filter
   * @param sort sort field (name, status, createdAt)
   * @param direction sort direction (asc, desc)
   * @return list of matching products
   */
  @Get
  @Secured({"CUSTOMER", "CATALOG"})
  @Operation(
      summary = "List products (v2)",
      description = "Lists products with filtering and sorting")
  @ApiResponse(responseCode = "200", description = "Matching products")
  @ApiResponse(responseCode = "400", description = "Invalid regular expression")
  public List<ProductResponse> list(
      @QueryValue @Nullable String sku,
      @QueryValue @Nullable String nameRegex,
      @QueryValue @Nullable ProductStatus status,
      @QueryValue(defaultValue = "name") String sort,
      @QueryValue(defaultValue = "asc") String direction) {
    ProductUseCase.ListProductsQuery query =
        new ProductUseCase.ListProductsQuery(sku, nameRegex, status, sort, direction);
    List<Product> products = productUseCase.listProducts(query);
    return ProductResponse.fromDomainList(
        products, resolveProductDiscountsUseCase.resolveActiveDiscounts(products));
  }
}
