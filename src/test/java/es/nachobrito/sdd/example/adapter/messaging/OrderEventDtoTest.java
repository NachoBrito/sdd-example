package es.nachobrito.sdd.example.adapter.messaging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.nachobrito.sdd.example.domain.model.DeliveryAddress;
import es.nachobrito.sdd.example.domain.model.InvoiceAddress;
import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderEventType;
import es.nachobrito.sdd.example.domain.model.OrderLifecycleEvent;
import es.nachobrito.sdd.example.domain.model.OrderLine;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderEventDtoTest {

  private Order order;
  private UUID orderId;
  private UUID userId;
  private UUID productId;

  @BeforeEach
  void setUp() {
    orderId = UUID.randomUUID();
    userId = UUID.randomUUID();
    productId = UUID.randomUUID();

    DeliveryAddress delivery = new DeliveryAddress(
        "Eve", "+123", "Street", "Suite 1", "City", "State", "123", "Country");
    InvoiceAddress invoice = new InvoiceAddress(
        "Eve", "VAT123", "+123", "Street", null, "City", "State", "123", "Country");
    OrderLine line = new OrderLine(productId, 2, 1000, 2000);

    order = new Order(
        orderId,
        userId,
        "Eve",
        "eve@example.com",
        Instant.now(),
        OrderStatus.PENDING,
        2000,
        "EUR",
        delivery,
        invoice,
        List.of(line),
        Instant.now(),
        Instant.now());
  }

  @Test
  void convertsOrderToDtoAndBackToDomain() {
    OrderEventDto dto = OrderEventDto.fromDomain(OrderEventType.CREATED, order);

    assertNotNull(dto.eventId());
    assertEquals("CREATED", dto.eventType());
    assertNotNull(dto.timestamp());
    assertEquals(orderId, dto.order().id());
    assertEquals(2000, dto.order().totalAmountInCents());
    assertEquals("Suite 1", dto.order().deliveryAddress().complement());
    assertNull(dto.order().invoiceAddress().complement());
    assertEquals(1, dto.order().orderLines().size());

    OrderLifecycleEvent domainEvent = dto.toDomain();
    assertEquals(dto.eventId(), domainEvent.eventId());
    assertEquals(OrderEventType.CREATED, domainEvent.eventType());
    assertEquals(orderId, domainEvent.order().id());
    assertEquals(2000, domainEvent.order().totalAmountInCents());
    assertEquals("Suite 1", domainEvent.order().deliveryAddress().complement());
    assertEquals(1, domainEvent.order().orderLines().size());
  }

  @Test
  void convertsAddressPayloadDtoToDeliveryAndInvoice() {
    AddressPayloadDto deliveryDto = AddressPayloadDto.fromDeliveryAddress(order.deliveryAddress());
    DeliveryAddress delivery = deliveryDto.toDeliveryAddress();
    assertEquals(order.deliveryAddress().name(), delivery.name());
    assertEquals(order.deliveryAddress().complement(), delivery.complement());

    AddressPayloadDto invoiceDto = AddressPayloadDto.fromInvoiceAddress(order.invoiceAddress());
    InvoiceAddress invoice = invoiceDto.toInvoiceAddress();
    assertEquals(order.invoiceAddress().name(), invoice.name());
    assertEquals("VAT123", invoice.vatNumber());
    assertNull(invoice.complement());

    AddressPayloadDto blankVatDto = new AddressPayloadDto(
        "Eve", "   ", "+123", "Street", null, "City", "State", "123", "Country");
    assertEquals("N/A", blankVatDto.toInvoiceAddress().vatNumber());

    AddressPayloadDto nullVatDto = new AddressPayloadDto(
        "Eve", null, "+123", "Street", null, "City", "State", "123", "Country");
    assertEquals("N/A", nullVatDto.toInvoiceAddress().vatNumber());
  }

  @Test
  void acceptsBoundaryValuesInPayloadDtos() {
    assertDoesNotThrow(() ->
        new OrderLinePayloadDto(productId, 1, 0, 0));

    AddressPayloadDto address = AddressPayloadDto.fromDeliveryAddress(order.deliveryAddress());
    OrderLinePayloadDto line = new OrderLinePayloadDto(productId, 1, 0, 0);

    assertDoesNotThrow(() ->
        new OrderPayloadDto(
            orderId, userId, "Eve", "eve@example.com", Instant.now(), "PENDING",
            0, "EUR", address, address, List.of(line)));

    OrderPayloadDto payload = OrderPayloadDto.fromDomain(order);
    Order domainOrder = payload.toDomain(null);
    assertEquals(order.date(), domainOrder.createdAt());
  }

  @Test
  void convertsOrderLinePayloadDtoToDomain() {
    OrderLine line = order.orderLines().get(0);
    OrderLinePayloadDto lineDto = OrderLinePayloadDto.fromDomain(line);
    OrderLine domainLine = lineDto.toDomain();

    assertEquals(line.productId(), domainLine.productId());
    assertEquals(line.quantity(), domainLine.quantity());
    assertEquals(line.unitPriceInCents(), domainLine.unitPriceInCents());
    assertEquals(line.lineTotalInCents(), domainLine.lineTotalInCents());
  }

  @Test
  void validatesAddressPayloadDtoRequiredFields() {
    assertThrows(NullPointerException.class, () ->
        new AddressPayloadDto(null, "+123", "Street", null, "City", "State", "123", "Country"));
    assertThrows(NullPointerException.class, () ->
        new AddressPayloadDto("Name", null, "Street", null, "City", "State", "123", "Country"));
    assertThrows(NullPointerException.class, () ->
        new AddressPayloadDto("Name", "+123", null, null, "City", "State", "123", "Country"));
    assertThrows(NullPointerException.class, () ->
        new AddressPayloadDto("Name", "+123", "Street", null, null, "State", "123", "Country"));
    assertThrows(NullPointerException.class, () ->
        new AddressPayloadDto("Name", "+123", "Street", null, "City", null, "123", "Country"));
    assertThrows(NullPointerException.class, () ->
        new AddressPayloadDto("Name", "+123", "Street", null, "City", "State", null, "Country"));
    assertThrows(NullPointerException.class, () ->
        new AddressPayloadDto("Name", "+123", "Street", null, "City", "State", "123", null));

    assertThrows(NullPointerException.class, () ->
        AddressPayloadDto.fromDeliveryAddress(null));
    assertThrows(NullPointerException.class, () ->
        AddressPayloadDto.fromInvoiceAddress(null));
  }

  @Test
  void validatesOrderLinePayloadDtoInvariants() {
    assertThrows(NullPointerException.class, () ->
        new OrderLinePayloadDto(null, 1, 100, 100));
    assertThrows(IllegalArgumentException.class, () ->
        new OrderLinePayloadDto(productId, 0, 100, 0));
    assertThrows(IllegalArgumentException.class, () ->
        new OrderLinePayloadDto(productId, 1, -1, 0));
    assertThrows(IllegalArgumentException.class, () ->
        new OrderLinePayloadDto(productId, 1, 100, -1));

    assertThrows(NullPointerException.class, () ->
        OrderLinePayloadDto.fromDomain(null));
  }

  @Test
  void validatesOrderPayloadDtoInvariants() {
    AddressPayloadDto address = AddressPayloadDto.fromDeliveryAddress(order.deliveryAddress());
    OrderLinePayloadDto line = OrderLinePayloadDto.fromDomain(order.orderLines().get(0));
    List<OrderLinePayloadDto> lines = List.of(line);
    Instant now = Instant.now();

    assertThrows(NullPointerException.class, () ->
        new OrderPayloadDto(
            null, userId, "Eve", "eve@example.com", now, "PENDING",
            2000, "EUR", address, address, lines));
    assertThrows(NullPointerException.class, () ->
        new OrderPayloadDto(
            orderId, userId, null, "eve@example.com", now, "PENDING",
            2000, "EUR", address, address, lines));
    assertThrows(NullPointerException.class, () ->
        new OrderPayloadDto(
            orderId, userId, "Eve", null, now, "PENDING",
            2000, "EUR", address, address, lines));
    assertThrows(NullPointerException.class, () ->
        new OrderPayloadDto(
            orderId, userId, "Eve", "eve@example.com", null, "PENDING",
            2000, "EUR", address, address, lines));
    assertThrows(NullPointerException.class, () ->
        new OrderPayloadDto(
            orderId, userId, "Eve", "eve@example.com", now, null,
            2000, "EUR", address, address, lines));
    assertThrows(NullPointerException.class, () ->
        new OrderPayloadDto(
            orderId, userId, "Eve", "eve@example.com", now, "PENDING",
            2000, null, address, address, lines));
    assertThrows(NullPointerException.class, () ->
        new OrderPayloadDto(
            orderId, userId, "Eve", "eve@example.com", now, "PENDING",
            2000, "EUR", null, address, lines));
    assertThrows(NullPointerException.class, () ->
        new OrderPayloadDto(
            orderId, userId, "Eve", "eve@example.com", now, "PENDING",
            2000, "EUR", address, null, lines));
    assertThrows(NullPointerException.class, () ->
        new OrderPayloadDto(
            orderId, userId, "Eve", "eve@example.com", now, "PENDING",
            2000, "EUR", address, address, null));
    assertThrows(IllegalArgumentException.class, () ->
        new OrderPayloadDto(
            orderId, userId, "Eve", "eve@example.com", now, "PENDING",
            2000, "EUR", address, address, List.of()));
    assertThrows(IllegalArgumentException.class, () ->
        new OrderPayloadDto(
            orderId, userId, "Eve", "eve@example.com", now, "PENDING",
            -1, "EUR", address, address, lines));

    assertThrows(NullPointerException.class, () ->
        OrderPayloadDto.fromDomain(null));
  }

  @Test
  void validatesOrderEventDtoInvariants() {
    OrderPayloadDto payload = OrderPayloadDto.fromDomain(order);
    UUID eventId = UUID.randomUUID();
    Instant now = Instant.now();

    assertThrows(NullPointerException.class, () ->
        new OrderEventDto(null, "CREATED", now, payload));
    assertThrows(NullPointerException.class, () ->
        new OrderEventDto(eventId, null, now, payload));
    assertThrows(NullPointerException.class, () ->
        new OrderEventDto(eventId, "CREATED", null, payload));
    assertThrows(NullPointerException.class, () ->
        new OrderEventDto(eventId, "CREATED", now, null));

    assertThrows(NullPointerException.class, () ->
        OrderEventDto.fromDomain(null, order));
    assertThrows(NullPointerException.class, () ->
        OrderEventDto.fromDomain(OrderEventType.CREATED, null));
  }
}

