package es.nachobrito.sdd.example.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Domain aggregate root representing an order.
 *
 * @param id the unique order identifier
 * @param userId the owning user identifier
 * @param customerName the customer name
 * @param customerEmail the customer email address
 * @param date the order placement timestamp
 * @param status the order status
 * @param totalAmountInCents the calculated total amount in integer cents
 * @param currency the three-letter ISO-4217 currency code
 * @param deliveryAddress the delivery address value object
 * @param invoiceAddress the invoice address value object
 * @param orderLines the non-empty list of order line value objects
 * @param createdAt the record creation timestamp in UTC
 * @param updatedAt the record last modification timestamp in UTC
 */
public record Order(
    UUID id,
    UUID userId,
    String customerName,
    String customerEmail,
    Instant date,
    OrderStatus status,
    long totalAmountInCents,
    String currency,
    DeliveryAddress deliveryAddress,
    InvoiceAddress invoiceAddress,
    List<OrderLine> orderLines,
    Instant createdAt,
    Instant updatedAt) {

  private static final int MAX_CUSTOMER_NAME_LENGTH = 255;
  private static final int MAX_CUSTOMER_EMAIL_LENGTH = 255;
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

  /**
   * Compact constructor that validates all domain invariants.
   */
  public Order {
    if (id == null) {
      throw new InvalidOrderDataException("Order id cannot be null");
    }
    if (userId == null) {
      throw new InvalidOrderDataException("Order userId cannot be null");
    }
    if (customerName == null || customerName.isBlank()) {
      throw new InvalidOrderDataException("Order customerName cannot be blank");
    }
    customerName = customerName.trim();
    if (customerName.length() > MAX_CUSTOMER_NAME_LENGTH) {
      throw new InvalidOrderDataException(
          "Order customerName cannot exceed " + MAX_CUSTOMER_NAME_LENGTH + " characters");
    }
    if (customerEmail == null || customerEmail.isBlank()) {
      throw new InvalidOrderDataException("Order customerEmail cannot be blank");
    }
    customerEmail = customerEmail.trim();
    if (customerEmail.length() > MAX_CUSTOMER_EMAIL_LENGTH) {
      throw new InvalidOrderDataException(
          "Order customerEmail cannot exceed " + MAX_CUSTOMER_EMAIL_LENGTH + " characters");
    }
    if (!EMAIL_PATTERN.matcher(customerEmail).matches()) {
      throw new InvalidOrderDataException("Invalid customer email format: " + customerEmail);
    }
    if (date == null) {
      throw new InvalidOrderDataException("Order date cannot be null");
    }
    if (status == null) {
      throw new InvalidOrderDataException("Order status cannot be null");
    }
    if (currency == null || currency.isBlank()) {
      throw new InvalidOrderDataException("Order currency cannot be blank");
    }
    currency = currency.trim().toUpperCase(java.util.Locale.ROOT);
    validateCurrency(currency);
    if (deliveryAddress == null) {
      throw new InvalidOrderDataException("Order deliveryAddress cannot be null");
    }
    if (invoiceAddress == null) {
      throw new InvalidOrderDataException("Order invoiceAddress cannot be null");
    }
    if (orderLines == null || orderLines.isEmpty()) {
      throw new InvalidOrderDataException("Order orderLines cannot be empty");
    }
    orderLines = List.copyOf(orderLines);
    long calculatedTotal = calculateTotal(orderLines);
    if (totalAmountInCents != calculatedTotal) {
      throw new InvalidOrderDataException("Order totalAmountInCents must match sum of lines");
    }
    if (createdAt == null) {
      throw new InvalidOrderDataException("Order createdAt cannot be null");
    }
    if (updatedAt == null) {
      throw new InvalidOrderDataException("Order updatedAt cannot be null");
    }
  }

  @Override
  public List<OrderLine> orderLines() {
    return Collections.unmodifiableList(orderLines);
  }

  /**
   * Calculates the total amount in cents from a list of order lines.
   *
   * @param lines the list of order lines
   * @return the total amount in integer cents
   */
  public static long calculateTotal(List<OrderLine> lines) {
    long total = 0L;
    if (lines != null) {
      for (OrderLine line : lines) {
        total += line.lineTotalInCents();
      }
    }
    return total;
  }

  /**
   * Creates a new Order instance from the given draft attributes.
   *
   * @param draft the order draft attributes
   * @return the new Order instance
   */
  public static Order create(OrderDraft draft) {
    long total = calculateTotal(draft.orderLines());
    return new Order(
        draft.id(),
        draft.userId(),
        draft.customerName(),
        draft.customerEmail(),
        draft.date(),
        draft.status(),
        total,
        draft.currency(),
        draft.deliveryAddress(),
        draft.invoiceAddress(),
        draft.orderLines(),
        draft.date(),
        draft.date());
  }

  /**
   * Draft parameters for creating a new order.
   *
   * @param id the unique order identifier
   * @param userId the owning user identifier
   * @param customerName the customer name
   * @param customerEmail the customer email
   * @param date the order date
   * @param status the order status
   * @param currency the currency code
   * @param deliveryAddress the delivery address
   * @param invoiceAddress the invoice address
   * @param orderLines the list of order lines
   */
  public record OrderDraft(
      UUID id,
      UUID userId,
      String customerName,
      String customerEmail,
      Instant date,
      OrderStatus status,
      String currency,
      DeliveryAddress deliveryAddress,
      InvoiceAddress invoiceAddress,
      List<OrderLine> orderLines) {

    /**
     * Creates a validated OrderDraft with an immutable order line list.
     */
    public OrderDraft {
      orderLines = orderLines == null ? List.of() : List.copyOf(orderLines);
    }

    /**
     * Returns an immutable copy of the order lines.
     *
     * @return the order lines
     */
    @Override
    public List<OrderLine> orderLines() {
      return List.copyOf(orderLines);
    }
  }

  /**
   * Returns a new Order instance with updated attributes and refreshed timestamp.
   *
   * @param newCustomerName the updated customer name
   * @param newCustomerEmail the updated customer email
   * @param newDate the updated order date
   * @param newStatus the updated order status
   * @param newCurrency the updated currency code
   * @param newDeliveryAddress the updated delivery address
   * @param newInvoiceAddress the updated invoice address
   * @param newOrderLines the updated order lines
   * @param newUpdatedAt the new update timestamp
   * @return the updated Order instance
   */
  public Order withUpdatedAttributes(
      String newCustomerName,
      String newCustomerEmail,
      Instant newDate,
      OrderStatus newStatus,
      String newCurrency,
      DeliveryAddress newDeliveryAddress,
      InvoiceAddress newInvoiceAddress,
      List<OrderLine> newOrderLines,
      Instant newUpdatedAt) {
    long newTotal = calculateTotal(newOrderLines);
    return new Order(
        this.id,
        this.userId,
        newCustomerName,
        newCustomerEmail,
        newDate,
        newStatus,
        newTotal,
        newCurrency,
        newDeliveryAddress,
        newInvoiceAddress,
        newOrderLines,
        this.createdAt,
        newUpdatedAt);
  }

  private static void validateCurrency(String currencyCode) {
    try {
      Currency.getInstance(currencyCode);
    } catch (IllegalArgumentException e) {
      throw new InvalidOrderDataException("Invalid currency code: " + currencyCode, e);
    }
  }
}
