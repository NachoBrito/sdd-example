package es.nachobrito.sdd.example.application.service;

import es.nachobrito.sdd.example.application.port.in.ProductUseCase;
import es.nachobrito.sdd.example.application.port.out.ProductRepositoryPort;
import es.nachobrito.sdd.example.domain.model.DuplicateIdentifierException;
import es.nachobrito.sdd.example.domain.model.DuplicateSkuException;
import es.nachobrito.sdd.example.domain.model.InvalidProductDataException;
import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductNotFoundException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Application service implementing product use cases.
 */
@Singleton
public class ProductService implements ProductUseCase {

  private final ProductRepositoryPort repository;
  private final Clock clock;

  /**
   * Constructs the service with default system UTC clock.
   *
   * @param repository the product repository port
   */
  @Inject
  public ProductService(ProductRepositoryPort repository) {
    this(repository, Clock.systemUTC());
  }

  /**
   * Constructs the service with a specific clock for testing.
   *
   * @param repository the product repository port
   * @param clock the clock instance
   */
  public ProductService(ProductRepositoryPort repository, Clock clock) {
    this.repository = repository;
    this.clock = clock;
  }

  @Override
  public Product createProduct(CreateProductCommand command) {
    Objects.requireNonNull(command, "Command cannot be null");
    if (repository.existsById(command.id())) {
      throw new DuplicateIdentifierException(command.id());
    }
    if (repository.existsBySku(command.sku())) {
      throw new DuplicateSkuException(command.sku());
    }

    Product product = Product.create(
        command.id(),
        command.sku(),
        command.name(),
        command.description(),
        command.priceInCents(),
        command.currency(),
        command.status(),
        clock.instant());

    return repository.save(product);
  }

  @Override
  public Product getProduct(UUID id) {
    Objects.requireNonNull(id, "Product id cannot be null");
    return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
  }

  @Override
  public Product updateProduct(UpdateProductCommand command) {
    Objects.requireNonNull(command, "Command cannot be null");
    Product existing = repository.findById(command.id())
        .orElseThrow(() -> new ProductNotFoundException(command.id()));

    if (repository.existsBySkuAndIdNot(command.sku(), command.id())) {
      throw new DuplicateSkuException(command.sku());
    }

    Product updated = existing.withUpdatedAttributes(
        command.sku(),
        command.name(),
        command.description(),
        command.priceInCents(),
        command.currency(),
        command.status(),
        clock.instant());

    return repository.save(updated);
  }

  @Override
  public void deleteProduct(UUID id) {
    Objects.requireNonNull(id, "Product id cannot be null");
    if (!repository.existsById(id)) {
      throw new ProductNotFoundException(id);
    }
    repository.deleteById(id);
  }

  @Override
  public List<Product> listProducts(ListProductsQuery query) {
    Objects.requireNonNull(query, "Query cannot be null");
    validateRegex(query.nameRegex());
    return repository.findAll(
        query.sku(),
        query.nameRegex(),
        query.status(),
        query.sortField(),
        query.direction());
  }

  private static void validateRegex(String regex) {
    if (regex != null && !regex.isBlank()) {
      try {
        Pattern.compile(regex);
      } catch (PatternSyntaxException e) {
        throw new InvalidProductDataException("Invalid name regular expression: " + regex, e);
      }
    }
  }
}

