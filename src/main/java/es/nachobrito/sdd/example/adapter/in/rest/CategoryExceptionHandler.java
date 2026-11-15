package es.nachobrito.sdd.example.adapter.in.rest;

import es.nachobrito.sdd.example.adapter.in.rest.dto.ErrorResponse;
import es.nachobrito.sdd.example.domain.model.CategoryException;
import es.nachobrito.sdd.example.domain.model.CategoryInUseException;
import es.nachobrito.sdd.example.domain.model.CategoryNotFoundException;
import es.nachobrito.sdd.example.domain.model.DuplicateCategoryIdentifierException;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

/**
 * Translates category domain exceptions into HTTP error responses.
 */
@Produces
@Singleton
@Requires(classes = {CategoryException.class, ExceptionHandler.class})
public class CategoryExceptionHandler
    implements ExceptionHandler<CategoryException, HttpResponse<ErrorResponse>> {

  /**
   * Handles category domain exceptions.
   *
   * @param request the HTTP request
   * @param exception the category exception
   * @return the HTTP error response
   */
  @Override
  public HttpResponse<ErrorResponse> handle(
      HttpRequest request, CategoryException exception) {
    if (exception instanceof CategoryNotFoundException) {
      return HttpResponse.notFound(new ErrorResponse(exception.getMessage()));
    }
    if (exception instanceof DuplicateCategoryIdentifierException
        || exception instanceof CategoryInUseException) {
      return HttpResponse.status(HttpStatus.CONFLICT)
          .body(new ErrorResponse(exception.getMessage()));
    }
    return HttpResponse.badRequest(new ErrorResponse(exception.getMessage()));
  }
}

