package es.nachobrito.sdd.example.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.nachobrito.sdd.example.adapter.in.rest.dto.ErrorResponse;
import es.nachobrito.sdd.example.domain.model.DuplicateIdentifierException;
import es.nachobrito.sdd.example.domain.model.DuplicateSkuException;
import es.nachobrito.sdd.example.domain.model.InvalidProductDataException;
import es.nachobrito.sdd.example.domain.model.ProductNotFoundException;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductExceptionHandlerTest {

  private ProductExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new ProductExceptionHandler();
  }

  @Test
  void onNotFoundReturnsNotFoundStatus() {
    HttpResponse<ErrorResponse> response =
        handler.onNotFound(new ProductNotFoundException(UUID.randomUUID()));
    assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
  }

  @Test
  void onDuplicateIdentifierReturnsConflictStatus() {
    HttpResponse<ErrorResponse> response =
        handler.onDuplicateIdentifier(new DuplicateIdentifierException(UUID.randomUUID()));
    assertEquals(HttpStatus.CONFLICT, response.getStatus());
  }

  @Test
  void onDuplicateSkuReturnsConflictStatus() {
    HttpResponse<ErrorResponse> response =
        handler.onDuplicateSku(new DuplicateSkuException("SKU-DUP"));
    assertEquals(HttpStatus.CONFLICT, response.getStatus());
  }

  @Test
  void onInvalidProductDataReturnsBadRequestStatus() {
    HttpResponse<ErrorResponse> response =
        handler.onInvalidProductData(new InvalidProductDataException("Invalid"));
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
  }

  @Test
  void onConstraintViolationReturnsBadRequestStatus() {
    HttpResponse<ErrorResponse> response =
        handler.onConstraintViolation(new ConstraintViolationException(Set.of()));
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
  }
}

