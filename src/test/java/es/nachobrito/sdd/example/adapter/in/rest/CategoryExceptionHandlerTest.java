package es.nachobrito.sdd.example.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.nachobrito.sdd.example.adapter.in.rest.dto.ErrorResponse;
import es.nachobrito.sdd.example.domain.model.CategoryInUseException;
import es.nachobrito.sdd.example.domain.model.CategoryNotFoundException;
import es.nachobrito.sdd.example.domain.model.DuplicateCategoryIdentifierException;
import es.nachobrito.sdd.example.domain.model.InvalidCategoryDataException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CategoryExceptionHandlerTest {

  private static final UUID ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

  private final CategoryExceptionHandler handler = new CategoryExceptionHandler();

  @Test
  void mapsCategoryExceptionsToExpectedHttpStatuses() {
    HttpRequest<?> request = HttpRequest.GET("/v1/categories");

    HttpResponse<ErrorResponse> notFound =
        handler.handle(request, new CategoryNotFoundException(ID));
    assertEquals(HttpStatus.NOT_FOUND, notFound.getStatus());

    HttpResponse<ErrorResponse> duplicate =
        handler.handle(request, new DuplicateCategoryIdentifierException(ID));
    assertEquals(HttpStatus.CONFLICT, duplicate.getStatus());

    HttpResponse<ErrorResponse> inUse =
        handler.handle(request, new CategoryInUseException(ID));
    assertEquals(HttpStatus.CONFLICT, inUse.getStatus());

    HttpResponse<ErrorResponse> invalid =
        handler.handle(request, new InvalidCategoryDataException("Invalid name"));
    assertEquals(HttpStatus.BAD_REQUEST, invalid.getStatus());
    assertEquals("Invalid name", invalid.body().message());
  }
}

