package es.nachobrito.sdd.example.application.port.in;

/**
 * Composite inbound port combining all product management use cases.
 */
public interface ProductUseCase extends
    CreateProductUseCase,
    GetProductUseCase,
    UpdateProductUseCase,
    DeleteProductUseCase,
    ListProductsUseCase {
}

