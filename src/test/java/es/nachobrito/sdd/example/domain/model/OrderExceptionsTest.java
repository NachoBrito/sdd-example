package es.nachobrito.sdd.example.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderExceptionsTest {

  private static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  private static final class TestOrderException extends OrderException {
    private static final long serialVersionUID = 1L;

    TestOrderException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  @Test
  void orderNotFoundExceptionHasExpectedMessage() {
    OrderNotFoundException ex = new OrderNotFoundException(ORDER_ID);
    assertEquals("Order not found with identifier: " + ORDER_ID, ex.getMessage());
  }

  @Test
  void duplicateOrderIdentifierExceptionHasExpectedMessage() {
    DuplicateOrderIdentifierException ex = new DuplicateOrderIdentifierException(ORDER_ID);
    assertEquals("Order already exists with identifier: " + ORDER_ID, ex.getMessage());
  }

  @Test
  void orderDeletionConflictExceptionHasExpectedMessage() {
    OrderDeletionConflictException ex =
        new OrderDeletionConflictException(ORDER_ID, OrderStatus.PAID);
    assertEquals("Cannot delete order " + ORDER_ID + " with status: PAID", ex.getMessage());
  }

  @Test
  void invalidOrderDataExceptionHasExpectedMessage() {
    InvalidOrderDataException ex = new InvalidOrderDataException("Invalid order data");
    assertEquals("Invalid order data", ex.getMessage());
  }

  @Test
  void invalidOrderDataExceptionHasExpectedCause() {
    Throwable cause = new IllegalArgumentException("Root cause");
    InvalidOrderDataException ex = new InvalidOrderDataException("Invalid order data", cause);
    assertSame(cause, ex.getCause());
  }

  @Test
  void orderExceptionWithCauseHasExpectedCause() {
    Throwable cause = new IllegalArgumentException("Root cause");
    OrderException ex = new TestOrderException("Failed", cause);
    assertSame(cause, ex.getCause());
  }
}
