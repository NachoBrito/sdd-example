package es.nachobrito.sdd.example.adapter.in.rest;

import es.nachobrito.sdd.example.adapter.in.rest.dto.ErrorResponse;
import es.nachobrito.sdd.example.domain.model.DuplicateOrderIdentifierException;
import es.nachobrito.sdd.example.domain.model.InvalidOrderDataException;
import es.nachobrito.sdd.example.domain.model.OrderDeletionConflictException;
import es.nachobrito.sdd.example.domain.model.OrderException;
import es.nachobrito.sdd.example.domain.model.OrderNotFoundException;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

/**
 * Translates order domain exceptions into HTTP error responses.
 */
@Produces
@Singleton
@Requires(classes = {OrderException.class, ExceptionHandler.class})
public class OrderExceptionHandler
    implements ExceptionHandler<OrderException, HttpResponse<ErrorResponse>> {

  /**
   * Handles order domain exceptions.
   *
   * @param request the HTTP request
   * @param exception the order exception
   * @return the HTTP error response
   */
  @Override
  public HttpResponse<ErrorResponse> handle(
      HttpRequest request, OrderException exception) {
    if (exception instanceof OrderNotFoundException) {
      return HttpResponse.notFound(new ErrorResponse(exception.getMessage()));
    }
    if (exception instanceof DuplicateOrderIdentifierException
        || exception instanceof OrderDeletionConflictException) {
      return HttpResponse.status(HttpStatus.CONFLICT)
          .body(new ErrorResponse(exception.getMessage()));
    }
    if (exception instanceof InvalidOrderDataException) {
      return HttpResponse.badRequest(new ErrorResponse(exception.getMessage()));
    }
    return HttpResponse.badRequest(new ErrorResponse(exception.getMessage()));
  }
}
