package es.nachobrito.sdd.example.adapter.in.rest;

import es.nachobrito.sdd.example.adapter.in.rest.dto.ErrorResponse;
import es.nachobrito.sdd.example.domain.model.DuplicateIdentifierException;
import es.nachobrito.sdd.example.domain.model.DuplicateSkuException;
import es.nachobrito.sdd.example.domain.model.InvalidProductDataException;
import es.nachobrito.sdd.example.domain.model.ProductNotFoundException;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import jakarta.validation.ConstraintViolationException;
import java.util.List;

/**
 * Controller providing global exception handlers for the REST API.
 */
@Controller("/v1/errors")
public class ProductExceptionHandler {

  /**
   * Handles missing product exceptions.
   *
   * @param exception the missing product exception
   * @return 404 Not Found response
   */
  @Error(global = true, exception = ProductNotFoundException.class)
  public HttpResponse<ErrorResponse> onNotFound(ProductNotFoundException exception) {
    return HttpResponse.notFound(new ErrorResponse(exception.getMessage()));
  }

  /**
   * Handles duplicate identifier exceptions.
   *
   * @param exception the duplicate identifier exception
   * @return 409 Conflict response
   */
  @Error(global = true, exception = DuplicateIdentifierException.class)
  public HttpResponse<ErrorResponse> onDuplicateIdentifier(DuplicateIdentifierException exception) {
    return HttpResponse.status(HttpStatus.CONFLICT).body(new ErrorResponse(exception.getMessage()));
  }

  /**
   * Handles duplicate SKU exceptions.
   *
   * @param exception the duplicate SKU exception
   * @return 409 Conflict response
   */
  @Error(global = true, exception = DuplicateSkuException.class)
  public HttpResponse<ErrorResponse> onDuplicateSku(DuplicateSkuException exception) {
    return HttpResponse.status(HttpStatus.CONFLICT).body(new ErrorResponse(exception.getMessage()));
  }

  /**
   * Handles invalid domain data exceptions.
   *
   * @param exception the invalid product data exception
   * @return 400 Bad Request response
   */
  @Error(global = true, exception = InvalidProductDataException.class)
  public HttpResponse<ErrorResponse> onInvalidProductData(InvalidProductDataException exception) {
    return HttpResponse.badRequest(new ErrorResponse(exception.getMessage()));
  }

  /**
   * Handles bean validation constraint exceptions.
   *
   * @param exception the validation exception
   * @return 400 Bad Request response with violation messages
   */
  @Error(global = true, exception = ConstraintViolationException.class)
  public HttpResponse<ErrorResponse> onConstraintViolation(ConstraintViolationException exception) {
    List<String> details = exception.getConstraintViolations().stream()
        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
        .toList();
    return HttpResponse.badRequest(new ErrorResponse("Validation failed", details));
  }
}

