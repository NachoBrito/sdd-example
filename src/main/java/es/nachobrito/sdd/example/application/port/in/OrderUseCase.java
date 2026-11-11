package es.nachobrito.sdd.example.application.port.in;

/**
 * Composite inbound port combining all order management use cases.
 */
public interface OrderUseCase extends
    CreateOrderUseCase,
    GetOrderUseCase,
    UpdateOrderUseCase,
    DeleteOrderUseCase,
    ListOrdersUseCase {
}

