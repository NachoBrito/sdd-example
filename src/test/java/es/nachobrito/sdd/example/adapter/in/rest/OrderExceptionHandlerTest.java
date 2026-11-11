package es.nachobrito.sdd.example.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.nachobrito.sdd.example.adapter.in.rest.dto.ErrorResponse;
import es.nachobrito.sdd.example.domain.model.DuplicateOrderIdentifierException;
import es.nachobrito.sdd.example.domain.model.InvalidOrderDataException;
import es.nachobrito.sdd.example.domain.model.OrderDeletionConflictException;
import es.nachobrito.sdd.example.domain.model.OrderNotFoundException;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderExceptionHandlerTest {

  private OrderExceptionHandler handler;
  private static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @BeforeEach
  void setUp() {
    handler = new OrderExceptionHandler();
  }

  @Test
  void onNotFoundReturnsNotFoundStatus() {
    HttpResponse<ErrorResponse> response =
        handler.onNotFound(new OrderNotFoundException(ORDER_ID));
    assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
  }

  @Test
  void onDuplicateIdentifierReturnsConflictStatus() {
    HttpResponse<ErrorResponse> response =
        handler.onDuplicateIdentifier(new DuplicateOrderIdentifierException(ORDER_ID));
    assertEquals(HttpStatus.CONFLICT, response.getStatus());
  }

  @Test
  void onDeletionConflictReturnsConflictStatus() {
    HttpResponse<ErrorResponse> response =
        handler.onDeletionConflict(new OrderDeletionConflictException(ORDER_ID, OrderStatus.PAID));
    assertEquals(HttpStatus.CONFLICT, response.getStatus());
  }

  @Test
  void onInvalidOrderDataReturnsBadRequestStatus() {
    HttpResponse<ErrorResponse> response =
        handler.onInvalidOrderData(new InvalidOrderDataException("Invalid order data"));
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
  }
}

