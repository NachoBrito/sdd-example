package es.nachobrito.sdd.example.adapter.in.rest;

import es.nachobrito.sdd.example.adapter.in.rest.dto.CreateOrderRequest;
import es.nachobrito.sdd.example.adapter.in.rest.dto.OrderResponse;
import es.nachobrito.sdd.example.adapter.in.rest.dto.UpdateOrderRequest;
import es.nachobrito.sdd.example.application.port.in.OrderUseCase;
import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
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
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * REST controller for managing order resources.
 */
@Validated
@Controller("/v1/orders")
@Tag(name = "Orders", description = "Endpoints for managing orders")
public class OrderController {

  private final OrderUseCase orderUseCase;

  /**
   * Constructs the controller with the order use cases.
   *
   * @param orderUseCase the combined order use case port
   */
  public OrderController(OrderUseCase orderUseCase) {
    this.orderUseCase = orderUseCase;
  }

  /**
   * Creates a new order with client-supplied identifier.
   *
   * @param request the creation request body
   * @return 201 Created with the order and Location header
   */
  @Post
  @Secured({"CUSTOMER"})
  @Operation(
      summary = "Create order",
      description = "Creates an order with client-generated identifier")
  @ApiResponse(responseCode = "201", description = "Order created successfully")
  @ApiResponse(responseCode = "400", description = "Validation error")
  @ApiResponse(responseCode = "409", description = "Order identifier already exists")
  public HttpResponse<OrderResponse> create(@Body @Valid CreateOrderRequest request) {
    var command = request.toCommand(null);
    Order created = orderUseCase.createOrder(command);
    URI location = URI.create("/v1/orders/" + created.id());
    return HttpResponse.created(OrderResponse.fromDomain(created), location);
  }

  /**
   * Retrieves an order by its identifier.
   *
   * @param id the order identifier
   * @return 200 OK with order details
   */
  @Get("/{id}")
  @Secured({"CUSTOMER", "ADMIN"})
  @Operation(summary = "Get order", description = "Retrieves an order by identifier")
  @ApiResponse(responseCode = "200", description = "Order found")
  @ApiResponse(responseCode = "404", description = "Order not found")
  public OrderResponse getById(@PathVariable UUID id) {
    Order order = orderUseCase.getOrder(id);
    return OrderResponse.fromDomain(order);
  }

  /**
   * Updates an existing order by its identifier.
   *
   * @param id the order identifier
   * @param request the update request body
   * @return 200 OK with updated order details
   */
  @Put("/{id}")
  @Secured({"ADMIN"})
  @Operation(summary = "Update order", description = "Updates an existing order by identifier")
  @ApiResponse(responseCode = "200", description = "Order updated")
  @ApiResponse(responseCode = "400", description = "Validation error")
  @ApiResponse(responseCode = "404", description = "Order not found")
  public OrderResponse update(
      @PathVariable UUID id,
      @Body @Valid UpdateOrderRequest request) {
    var command = request.toCommand(id);
    Order updated = orderUseCase.updateOrder(command);
    return OrderResponse.fromDomain(updated);
  }

  /**
   * Permanently deletes an order by its identifier.
   *
   * @param id the order identifier
   * @return 204 No Content
   */
  @Delete("/{id}")
  @Secured({"ADMIN"})
  @Operation(
      summary = "Delete order",
      description = "Permanently deletes an order with PENDING or CANCELLED status")
  @ApiResponse(responseCode = "204", description = "Order permanently deleted")
  @ApiResponse(responseCode = "404", description = "Order not found")
  @ApiResponse(responseCode = "409", description = "Order cannot be deleted due to status")
  public HttpResponse<Void> delete(@PathVariable UUID id) {
    orderUseCase.deleteOrder(id);
    return HttpResponse.noContent();
  }

  /**
   * Lists orders with optional filtering and sorting.
   *
   * @param status optional status filter
   * @param customerEmail optional customer email filter
   * @param startDate optional start date filter
   * @param endDate optional end date filter
   * @param sort sort field (date, status)
   * @param direction sort direction (asc, desc)
   * @return list of matching orders
   */
  @Get
  @Secured({"CUSTOMER", "ADMIN"})
  @Operation(summary = "List orders", description = "Lists orders with filtering and sorting")
  @ApiResponse(responseCode = "200", description = "Matching orders")
  @ApiResponse(responseCode = "400", description = "Invalid query parameters")
  public List<OrderResponse> list(
      @QueryValue @Nullable OrderStatus status,
      @QueryValue @Nullable String customerEmail,
      @QueryValue @Nullable Instant startDate,
      @QueryValue @Nullable Instant endDate,
      @QueryValue(defaultValue = "date") String sort,
      @QueryValue(defaultValue = "asc") String direction) {
    var query =
        new OrderUseCase.ListOrdersQuery(
            status, customerEmail, startDate, endDate, sort, direction);
    return orderUseCase.listOrders(query).stream()
        .map(OrderResponse::fromDomain)
        .toList();
  }
}

