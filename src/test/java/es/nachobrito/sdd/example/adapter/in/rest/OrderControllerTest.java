package es.nachobrito.sdd.example.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.adapter.in.rest.dto.CreateOrderRequest;
import es.nachobrito.sdd.example.adapter.in.rest.dto.DeliveryAddressDto;
import es.nachobrito.sdd.example.adapter.in.rest.dto.InvoiceAddressDto;
import es.nachobrito.sdd.example.adapter.in.rest.dto.OrderLineRequest;
import es.nachobrito.sdd.example.adapter.in.rest.dto.OrderResponse;
import es.nachobrito.sdd.example.adapter.in.rest.dto.UpdateOrderRequest;
import es.nachobrito.sdd.example.application.port.in.CreateOrderUseCase.CreateOrderCommand;
import es.nachobrito.sdd.example.application.port.in.ListOrdersUseCase.ListOrdersQuery;
import es.nachobrito.sdd.example.application.port.in.OrderUseCase;
import es.nachobrito.sdd.example.application.port.in.UpdateOrderUseCase.UpdateOrderCommand;
import es.nachobrito.sdd.example.domain.model.DeliveryAddress;
import es.nachobrito.sdd.example.domain.model.InvoiceAddress;
import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderLine;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

  @Mock
  private OrderUseCase orderUseCase;

  private OrderController controller;

  private static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");
  private static final DeliveryAddressDto DELIVERY_DTO = new DeliveryAddressDto(
      "Alice Smith", "+1-555-123-4567", "123 Main St", "Apt 4B",
      "Springfield", "IL", "62701", "USA");
  private static final InvoiceAddressDto INVOICE_DTO = new InvoiceAddressDto(
      "Acme Corp", "US123456789", "+1-555-987-6543", "456 Market St", null,
      "Chicago", "IL", "60601", "USA");
  private static final DeliveryAddress DELIVERY = DELIVERY_DTO.toDomain();
  private static final InvoiceAddress INVOICE = INVOICE_DTO.toDomain();
  private static final Order SAMPLE_ORDER = Order.create(
      ORDER_ID, "John Doe", "john@example.com", NOW, OrderStatus.PENDING, "USD",
      DELIVERY, INVOICE, List.of(new OrderLine(PRODUCT_ID, 2, 5_000)));

  @BeforeEach
  void setUp() {
    controller = new OrderController(orderUseCase);
  }

  @Test
  void createReturnsCreatedResponse() {
    CreateOrderRequest request = new CreateOrderRequest(
        ORDER_ID, "John Doe", "john@example.com", NOW, OrderStatus.PENDING, 10_000L, "USD",
        DELIVERY_DTO, INVOICE_DTO,
        List.of(new OrderLineRequest(PRODUCT_ID, 2, 5_000, 10_000L)));

    when(orderUseCase.createOrder(any(CreateOrderCommand.class))).thenReturn(SAMPLE_ORDER);

    HttpResponse<OrderResponse> response = controller.create(request);

    assertEquals(HttpStatus.CREATED, response.getStatus());
  }

  @Test
  void getByIdReturnsOrderResponse() {
    when(orderUseCase.getOrder(ORDER_ID)).thenReturn(SAMPLE_ORDER);

    OrderResponse response = controller.getById(ORDER_ID);

    assertEquals(ORDER_ID, response.id());
  }

  @Test
  void updateReturnsUpdatedOrderResponse() {
    UpdateOrderRequest request = new UpdateOrderRequest(
        "Jane Doe", "jane@example.com", NOW, OrderStatus.PAID, "USD",
        DELIVERY_DTO, INVOICE_DTO,
        List.of(new OrderLineRequest(PRODUCT_ID, 3, 5_000, 15_000L)));

    Order updated = Order.create(
        ORDER_ID, "Jane Doe", "jane@example.com", NOW, OrderStatus.PAID, "USD",
        DELIVERY, INVOICE, List.of(new OrderLine(PRODUCT_ID, 3, 5_000)));

    when(orderUseCase.updateOrder(any(UpdateOrderCommand.class))).thenReturn(updated);

    OrderResponse response = controller.update(ORDER_ID, request);

    assertEquals("Jane Doe", response.customerName());
  }

  @Test
  void deleteReturnsNoContentResponse() {
    HttpResponse<Void> response = controller.delete(ORDER_ID);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
  }

  @Test
  void deleteDelegatesToUseCase() {
    controller.delete(ORDER_ID);

    verify(orderUseCase).deleteOrder(ORDER_ID);
  }

  @Test
  void listReturnsListOfOrderResponses() {
    when(orderUseCase.listOrders(any(ListOrdersQuery.class))).thenReturn(List.of(SAMPLE_ORDER));

    List<OrderResponse> responses = controller.list(null, null, null, null, "date", "asc");

    assertEquals(1, responses.size());
  }
}

