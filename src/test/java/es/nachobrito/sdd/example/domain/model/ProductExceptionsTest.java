package es.nachobrito.sdd.example.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProductExceptionsTest {

  private static final UUID PRODUCT_ID =
      UUID.fromString("22222222-2222-2222-2222-222222222222");

  private static final class TestProductException extends ProductException {
    private static final long serialVersionUID = 1L;

    TestProductException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  @Test
  void productNotFoundExceptionHasExpectedMessage() {
    ProductNotFoundException ex = new ProductNotFoundException(PRODUCT_ID);
    assertEquals("Product not found with identifier: " + PRODUCT_ID, ex.getMessage());
  }

  @Test
  void duplicateIdentifierExceptionHasExpectedMessage() {
    DuplicateIdentifierException ex = new DuplicateIdentifierException(PRODUCT_ID);
    assertEquals("Product already exists with identifier: " + PRODUCT_ID, ex.getMessage());
  }

  @Test
  void duplicateSkuExceptionHasExpectedMessage() {
    DuplicateSkuException ex = new DuplicateSkuException("SKU-123");
    assertEquals("Product already exists with SKU: SKU-123", ex.getMessage());
  }

  @Test
  void invalidProductDataExceptionHasExpectedMessage() {
    InvalidProductDataException ex = new InvalidProductDataException("Invalid product");
    assertEquals("Invalid product", ex.getMessage());
  }

  @Test
  void invalidProductDataExceptionHasExpectedCause() {
    Throwable cause = new IllegalArgumentException("Bad value");
    InvalidProductDataException ex = new InvalidProductDataException("Invalid product", cause);
    assertSame(cause, ex.getCause());
  }

  @Test
  void productExceptionWithCauseHasExpectedCause() {
    Throwable cause = new IllegalArgumentException("Root cause");
    ProductException ex = new TestProductException("Failed", cause);
    assertSame(cause, ex.getCause());
  }
}
