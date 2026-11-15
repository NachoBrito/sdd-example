package es.nachobrito.sdd.example.application.port.in;

/**
 * Composite inbound port combining all category management use cases.
 */
public interface CategoryUseCase
    extends CreateCategoryUseCase, ListCategoriesUseCase, DeleteCategoryUseCase {
}

