package es.nachobrito.sdd.example.adapter.in.rest;

import es.nachobrito.sdd.example.adapter.in.rest.dto.ErrorResponse;
import es.nachobrito.sdd.example.domain.model.DuplicateIdentifierException;
import es.nachobrito.sdd.example.domain.model.DuplicateSkuException;
import es.nachobrito.sdd.example.domain.model.InvalidProductDataException;
import es.nachobrito.sdd.example.domain.model.ProductException;
import es.nachobrito.sdd.example.domain.model.ProductNotFoundException;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

/**
 * Translates product domain exceptions into HTTP error responses.
 */
@Produces
@Singleton
@Requires(classes = {ProductException.class, ExceptionHandler.class})
public class ProductExceptionHandler
    implements ExceptionHandler<ProductException, HttpResponse<ErrorResponse>> {

  /**
   * Handles product domain exceptions.
   *
   * @param request the HTTP request
   * @param exception the product exception
   * @return the HTTP error response
   */
  @Override
  public HttpResponse<ErrorResponse> handle(
      HttpRequest request, ProductException exception) {
    if (exception instanceof ProductNotFoundException) {
      return HttpResponse.notFound(new ErrorResponse(exception.getMessage()));
    }
    if (exception instanceof DuplicateIdentifierException
        || exception instanceof DuplicateSkuException) {
      return HttpResponse.status(HttpStatus.CONFLICT)
          .body(new ErrorResponse(exception.getMessage()));
    }
    if (exception instanceof InvalidProductDataException) {
      return HttpResponse.badRequest(new ErrorResponse(exception.getMessage()));
    }
    return HttpResponse.badRequest(new ErrorResponse(exception.getMessage()));
  }
}
