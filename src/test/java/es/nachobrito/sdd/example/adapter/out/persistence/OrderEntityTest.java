package es.nachobrito.sdd.example.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.nachobrito.sdd.example.domain.model.DeliveryAddress;
import es.nachobrito.sdd.example.domain.model.InvoiceAddress;
import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderLine;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderEntityTest {

  private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID PROD_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");

  @Test
  void fromDomainMapsAllFieldsCorrectly() {
    Order order = sampleOrder();
    OrderEntity entity = OrderEntity.fromDomain(order);

    assertEquals(order.customerEmail(), entity.customerEmail());
  }

  @Test
  void toDomainMapsAllFieldsCorrectly() {
    Order order = sampleOrder();
    OrderEntity entity = OrderEntity.fromDomain(order);
    Order mapped = entity.toDomain(order.orderLines());

    assertEquals(order.totalAmountInCents(), mapped.totalAmountInCents());
  }

  private static Order sampleOrder() {
    DeliveryAddress delivery = new DeliveryAddress(
        "John Doe", "123456789", "Main St 1", "Apt 2", "City", "State", "12345", "Country");
    InvoiceAddress invoice = new InvoiceAddress(
        "Company Inc", "VAT123", "987654321", "Market St 5", null, "City", "State",
        "12345", "Country");
    OrderLine line = new OrderLine(PROD_ID, 2, 2_500);
    return new Order(
        ID, "John Doe", "john@example.com", NOW, OrderStatus.PENDING, 5_000, "EUR",
        delivery, invoice, List.of(line), NOW, NOW);
  }
}

