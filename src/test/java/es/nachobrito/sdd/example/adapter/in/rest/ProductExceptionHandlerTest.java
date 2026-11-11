package es.nachobrito.sdd.example.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.nachobrito.sdd.example.adapter.in.rest.dto.ErrorResponse;
import es.nachobrito.sdd.example.domain.model.DuplicateIdentifierException;
import es.nachobrito.sdd.example.domain.model.DuplicateSkuException;
import es.nachobrito.sdd.example.domain.model.InvalidProductDataException;
import es.nachobrito.sdd.example.domain.model.ProductException;
import es.nachobrito.sdd.example.domain.model.ProductNotFoundException;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductExceptionHandlerTest {

  private ProductExceptionHandler handler;
  private static final UUID PRODUCT_ID =
      UUID.fromString("33333333-3333-3333-3333-333333333333");

  private static final class TestGenericProductException extends ProductException {
    private static final long serialVersionUID = 1L;

    TestGenericProductException(String message) {
      super(message);
    }
  }

  @BeforeEach
  void setUp() {
    handler = new ProductExceptionHandler();
  }

  @Test
  void onNotFoundReturnsNotFoundStatus() {
    HttpResponse<ErrorResponse> response =
        handler.handle(null, new ProductNotFoundException(PRODUCT_ID));
    assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
  }

  @Test
  void onNotFoundReturnsExpectedMessage() {
    HttpResponse<ErrorResponse> response =
        handler.handle(null, new ProductNotFoundException(PRODUCT_ID));
    assertEquals(
        "Product not found with identifier: " + PRODUCT_ID,
        response.getBody().orElseThrow().message());
  }

  @Test
  void onDuplicateIdentifierReturnsConflictStatus() {
    HttpResponse<ErrorResponse> response =
        handler.handle(null, new DuplicateIdentifierException(PRODUCT_ID));
    assertEquals(HttpStatus.CONFLICT, response.getStatus());
  }

  @Test
  void onDuplicateIdentifierReturnsExpectedMessage() {
    HttpResponse<ErrorResponse> response =
        handler.handle(null, new DuplicateIdentifierException(PRODUCT_ID));
    assertEquals(
        "Product already exists with identifier: " + PRODUCT_ID,
        response.getBody().orElseThrow().message());
  }

  @Test
  void onDuplicateSkuReturnsConflictStatus() {
    HttpResponse<ErrorResponse> response =
        handler.handle(null, new DuplicateSkuException("SKU-DUP"));
    assertEquals(HttpStatus.CONFLICT, response.getStatus());
  }

  @Test
  void onDuplicateSkuReturnsExpectedMessage() {
    HttpResponse<ErrorResponse> response =
        handler.handle(null, new DuplicateSkuException("SKU-DUP"));
    assertEquals(
        "Product already exists with SKU: SKU-DUP",
        response.getBody().orElseThrow().message());
  }

  @Test
  void onInvalidProductDataReturnsBadRequestStatus() {
    HttpResponse<ErrorResponse> response =
        handler.handle(null, new InvalidProductDataException("Invalid data"));
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
  }

  @Test
  void onInvalidProductDataReturnsExpectedMessage() {
    HttpResponse<ErrorResponse> response =
        handler.handle(null, new InvalidProductDataException("Invalid data"));
    assertEquals("Invalid data", response.getBody().orElseThrow().message());
  }

  @Test
  void onGenericProductExceptionReturnsBadRequestStatus() {
    ProductException genericException = new TestGenericProductException("Generic failure");
    HttpResponse<ErrorResponse> response = handler.handle(null, genericException);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
  }

  @Test
  void onGenericProductExceptionReturnsExpectedMessage() {
    ProductException genericException = new TestGenericProductException("Generic failure");
    HttpResponse<ErrorResponse> response = handler.handle(null, genericException);
    assertEquals("Generic failure", response.getBody().orElseThrow().message());
  }
}
