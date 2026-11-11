package es.nachobrito.sdd.example.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.nachobrito.sdd.example.adapter.in.rest.dto.ErrorResponse;
import es.nachobrito.sdd.example.domain.model.DuplicateOrderIdentifierException;
import es.nachobrito.sdd.example.domain.model.InvalidOrderDataException;
import es.nachobrito.sdd.example.domain.model.OrderDeletionConflictException;
import es.nachobrito.sdd.example.domain.model.OrderException;
import es.nachobrito.sdd.example.domain.model.OrderNotFoundException;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderExceptionHandlerTest {

  private OrderExceptionHandler handler;
  private static final UUID ORDER_ID =
      UUID.fromString("44444444-4444-4444-4444-444444444444");

  private static final class TestGenericOrderException extends OrderException {
    private static final long serialVersionUID = 1L;

    TestGenericOrderException(String message) {
      super(message);
    }
  }

  @BeforeEach
  void setUp() {
    handler = new OrderExceptionHandler();
  }

  @Test
  void onNotFoundReturnsNotFoundStatus() {
    HttpResponse<ErrorResponse> response =
        handler.handle(null, new OrderNotFoundException(ORDER_ID));
    assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
  }

  @Test
  void onNotFoundReturnsExpectedMessage() {
    HttpResponse<ErrorResponse> response =
        handler.handle(null, new OrderNotFoundException(ORDER_ID));
    assertEquals(
        "Order not found with identifier: " + ORDER_ID,
        response.getBody().orElseThrow().message());
  }

  @Test
  void onDuplicateIdentifierReturnsConflictStatus() {
    HttpResponse<ErrorResponse> response =
        handler.handle(null, new DuplicateOrderIdentifierException(ORDER_ID));
    assertEquals(HttpStatus.CONFLICT, response.getStatus());
  }

  @Test
  void onDuplicateIdentifierReturnsExpectedMessage() {
    HttpResponse<ErrorResponse> response =
        handler.handle(null, new DuplicateOrderIdentifierException(ORDER_ID));
    assertEquals(
        "Order already exists with identifier: " + ORDER_ID,
        response.getBody().orElseThrow().message());
  }

  @Test
  void onDeletionConflictReturnsConflictStatus() {
    HttpResponse<ErrorResponse> response =
        handler.handle(null, new OrderDeletionConflictException(ORDER_ID, OrderStatus.PAID));
    assertEquals(HttpStatus.CONFLICT, response.getStatus());
  }

  @Test
  void onDeletionConflictReturnsExpectedMessage() {
    HttpResponse<ErrorResponse> response =
        handler.handle(null, new OrderDeletionConflictException(ORDER_ID, OrderStatus.PAID));
    assertEquals(
        "Cannot delete order " + ORDER_ID + " with status: PAID",
        response.getBody().orElseThrow().message());
  }

  @Test
  void onInvalidOrderDataReturnsBadRequestStatus() {
    HttpResponse<ErrorResponse> response =
        handler.handle(null, new InvalidOrderDataException("Invalid order data"));
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
  }

  @Test
  void onInvalidOrderDataReturnsExpectedMessage() {
    HttpResponse<ErrorResponse> response =
        handler.handle(null, new InvalidOrderDataException("Invalid order data"));
    assertEquals("Invalid order data", response.getBody().orElseThrow().message());
  }

  @Test
  void onGenericOrderExceptionReturnsBadRequestStatus() {
    OrderException genericException = new TestGenericOrderException("Generic order error");
    HttpResponse<ErrorResponse> response = handler.handle(null, genericException);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
  }

  @Test
  void onGenericOrderExceptionReturnsExpectedMessage() {
    OrderException genericException = new TestGenericOrderException("Generic order error");
    HttpResponse<ErrorResponse> response = handler.handle(null, genericException);
    assertEquals("Generic order error", response.getBody().orElseThrow().message());
  }
}
