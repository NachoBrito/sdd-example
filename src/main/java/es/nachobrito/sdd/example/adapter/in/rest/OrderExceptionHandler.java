package es.nachobrito.sdd.example.adapter.in.rest;

import es.nachobrito.sdd.example.adapter.in.rest.dto.ErrorResponse;
import es.nachobrito.sdd.example.domain.model.DuplicateOrderIdentifierException;
import es.nachobrito.sdd.example.domain.model.InvalidOrderDataException;
import es.nachobrito.sdd.example.domain.model.OrderDeletionConflictException;
import es.nachobrito.sdd.example.domain.model.OrderNotFoundException;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;

/**
 * Controller providing global exception handlers for the Order REST API.
 */
@Controller("/v1/order-errors")
public class OrderExceptionHandler {

  /**
   * Handles missing order exceptions.
   *
   * @param exception the missing order exception
   * @return 404 Not Found response
   */
  @Error(global = true, exception = OrderNotFoundException.class)
  public HttpResponse<ErrorResponse> onNotFound(OrderNotFoundException exception) {
    return HttpResponse.notFound(new ErrorResponse(exception.getMessage()));
  }

  /**
   * Handles duplicate order identifier exceptions.
   *
   * @param exception the duplicate identifier exception
   * @return 409 Conflict response
   */
  @Error(global = true, exception = DuplicateOrderIdentifierException.class)
  public HttpResponse<ErrorResponse> onDuplicateIdentifier(
      DuplicateOrderIdentifierException exception) {
    return HttpResponse.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse(exception.getMessage()));
  }

  /**
   * Handles conflict when deleting non-deletable orders.
   *
   * @param exception the deletion conflict exception
   * @return 409 Conflict response
   */
  @Error(global = true, exception = OrderDeletionConflictException.class)
  public HttpResponse<ErrorResponse> onDeletionConflict(
      OrderDeletionConflictException exception) {
    return HttpResponse.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse(exception.getMessage()));
  }

  /**
   * Handles invalid order domain data exceptions.
   *
   * @param exception the invalid order data exception
   * @return 400 Bad Request response
   */
  @Error(global = true, exception = InvalidOrderDataException.class)
  public HttpResponse<ErrorResponse> onInvalidOrderData(
      InvalidOrderDataException exception) {
    return HttpResponse.badRequest(new ErrorResponse(exception.getMessage()));
  }
}

