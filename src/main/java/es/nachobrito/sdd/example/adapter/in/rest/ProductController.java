package es.nachobrito.sdd.example.adapter.in.rest;

import es.nachobrito.sdd.example.adapter.in.rest.dto.CreateProductRequest;
import es.nachobrito.sdd.example.adapter.in.rest.dto.ProductResponse;
import es.nachobrito.sdd.example.adapter.in.rest.dto.UpdateProductRequest;
import es.nachobrito.sdd.example.application.port.in.CreateProductUseCase.CreateProductCommand;
import es.nachobrito.sdd.example.application.port.in.ListProductsUseCase.ListProductsQuery;
import es.nachobrito.sdd.example.application.port.in.ProductUseCase;
import es.nachobrito.sdd.example.application.port.in.UpdateProductUseCase.UpdateProductCommand;
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
 * REST controller for managing product catalog resources.
 */
@Validated
@Controller("/v1/products")
@Tag(name = "Products", description = "Endpoints for managing products in the catalog")
public class ProductController {

  private final ProductUseCase productUseCase;

  /**
   * Constructs the controller with the product use cases.
   *
   * @param productUseCase the combined product use case port
   */
  public ProductController(ProductUseCase productUseCase) {
    this.productUseCase = productUseCase;
  }

  /**
   * Creates a new product with client-generated identifier.
   *
   * @param request the creation request body
   * @return 201 Created with the product and Location header
   */
  @Post
  @Operation(
      summary = "Create product",
      description = "Creates a product with client-generated identifier")
  @ApiResponse(responseCode = "201", description = "Product created successfully")
  @ApiResponse(responseCode = "400", description = "Validation error")
  @ApiResponse(responseCode = "409", description = "Identifier or SKU already exists")
  public HttpResponse<ProductResponse> create(@Body @Valid CreateProductRequest request) {
    CreateProductCommand command = new CreateProductCommand(
        request.id(),
        request.sku(),
        request.name(),
        request.description(),
        request.priceInCents(),
        request.currency(),
        request.status());

    Product created = productUseCase.createProduct(command);
    URI location = URI.create("/v1/products/" + created.id());
    return HttpResponse.created(ProductResponse.fromDomain(created), location);
  }

  /**
   * Retrieves a product by identifier.
   *
   * @param id the product identifier
   * @return 200 OK with product details
   */
  @Get("/{id}")
  @Operation(summary = "Get product", description = "Retrieves a product by identifier")
  @ApiResponse(responseCode = "200", description = "Product found")
  @ApiResponse(responseCode = "404", description = "Product not found")
  public ProductResponse getById(@PathVariable UUID id) {
    Product product = productUseCase.getProduct(id);
    return ProductResponse.fromDomain(product);
  }

  /**
   * Updates an existing product by identifier.
   *
   * @param id the product identifier
   * @param request the update request body
   * @return 200 OK with updated product
   */
  @Put("/{id}")
  @Operation(
      summary = "Update product",
      description = "Updates an existing product by identifier")
  @ApiResponse(responseCode = "200", description = "Product updated")
  @ApiResponse(responseCode = "400", description = "Validation error")
  @ApiResponse(responseCode = "404", description = "Product not found")
  @ApiResponse(responseCode = "409", description = "SKU already exists")
  public ProductResponse update(
      @PathVariable UUID id,
      @Body @Valid UpdateProductRequest request) {
    UpdateProductCommand command = new UpdateProductCommand(
        id,
        request.sku(),
        request.name(),
        request.description(),
        request.priceInCents(),
        request.currency(),
        request.status());

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
  @Operation(
      summary = "Delete product",
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
  @Operation(
      summary = "List products",
      description = "Lists products with filtering and sorting")
  @ApiResponse(responseCode = "200", description = "Matching products")
  @ApiResponse(responseCode = "400", description = "Invalid regular expression")
  public List<ProductResponse> list(
      @QueryValue @Nullable String sku,
      @QueryValue @Nullable String nameRegex,
      @QueryValue @Nullable ProductStatus status,
      @QueryValue(defaultValue = "name") String sort,
      @QueryValue(defaultValue = "asc") String direction) {
    ListProductsQuery query = new ListProductsQuery(sku, nameRegex, status, sort, direction);
    return productUseCase.listProducts(query).stream()
        .map(ProductResponse::fromDomain)
        .toList();
  }
}
