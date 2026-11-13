package es.nachobrito.sdd.example.adapter.in.rest.dto;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.nachobrito.sdd.example.application.port.in.CreateOrderUseCase.CreateOrderCommand;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateOrderRequestTest {

  private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID PROD_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");
  private static final DeliveryAddressDto DELIVERY = new DeliveryAddressDto(
      "John Doe", "123456789", "Main St 1", "Apt 2", "City", "State", "12345", "Country");
  private static final InvoiceAddressDto INVOICE = new InvoiceAddressDto(
      "Company Inc", "VAT123", "987654321", "Market St 5", null, "City", "State",
      "12345", "Country");
  private static final OrderLineRequest LINE = new OrderLineRequest(PROD_ID, 2, 2_500, null);

  @Test
  void toCommandMapsAllFields() {
    CreateOrderRequest request = new CreateOrderRequest(
        ID, "John Doe", "john@example.com", NOW, OrderStatus.PENDING, 5_000L, "EUR",
        DELIVERY, INVOICE, List.of(LINE));

    CreateOrderCommand command = request.toCommand(USER_ID);

    assertAll(
        () -> assertEquals(ID, command.id()),
        () -> assertEquals(USER_ID, command.userId()),
        () -> assertEquals("John Doe", command.customerName()),
        () -> assertEquals("john@example.com", command.customerEmail()),
        () -> assertEquals(NOW, command.date()),
        () -> assertEquals(OrderStatus.PENDING, command.status()),
        () -> assertEquals("EUR", command.currency()),
        () -> assertEquals(1, command.orderLines().size()));
  }

  @Test
  void orderLinesReturnsUnmodifiableList() {
    CreateOrderRequest request = new CreateOrderRequest(
        ID, "John Doe", "john@example.com", NOW, OrderStatus.PENDING, 5_000L, "EUR",
        DELIVERY, INVOICE, List.of(LINE));

    assertThrows(UnsupportedOperationException.class, () ->
        request.orderLines().add(LINE));
  }

  @Test
  void nullOrderLinesDefaultsToEmptyList() {
    CreateOrderRequest request = new CreateOrderRequest(
        ID, "John Doe", "john@example.com", NOW, OrderStatus.PENDING, 5_000L, "EUR",
        DELIVERY, INVOICE, null);

    assertTrue(request.orderLines().isEmpty());
  }

  @Test
  void orderLinesContainsElements() {
    CreateOrderRequest request = new CreateOrderRequest(
        ID, "John Doe", "john@example.com", NOW, OrderStatus.PENDING, 5_000L, "EUR",
        DELIVERY, INVOICE, List.of(LINE));

    assertEquals(1, request.orderLines().size());
  }
}

