package es.nachobrito.sdd.example.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.domain.model.DeliveryAddress;
import es.nachobrito.sdd.example.domain.model.InvoiceAddress;
import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderLine;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderPersistenceAdapterCommandTest {

  @Mock
  private OrderJdbcRepository orderRepository;

  @Mock
  private OrderLineJdbcRepository orderLineRepository;

  private OrderPersistenceAdapter adapter;

  private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID PROD_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");

  @BeforeEach
  void setUp() {
    adapter = new OrderPersistenceAdapter(orderRepository, orderLineRepository);
  }

  @Test
  void saveInsertsEntityAndLines() {
    Order order = sampleOrder();
    OrderEntity entity = OrderEntity.fromDomain(order);
    when(orderRepository.insert(any(OrderEntity.class))).thenReturn(entity);

    Order saved = adapter.save(order);

    assertEquals(order, saved);
  }

  @Test
  void saveInsertsOrderLinesIntoRepository() {
    Order order = sampleOrder();
    OrderEntity entity = OrderEntity.fromDomain(order);
    when(orderRepository.insert(any(OrderEntity.class))).thenReturn(entity);

    adapter.save(order);

    verify(orderLineRepository).insert(any(OrderLineEntity.class));
  }

  @Test
  void updateUpdatesEntityAndLines() {
    Order order = sampleOrder();
    OrderEntity entity = OrderEntity.fromDomain(order);
    when(orderRepository.update(any(OrderEntity.class))).thenReturn(entity);

    Order updated = adapter.update(order);

    assertEquals(order, updated);
  }

  @Test
  void updateDeletesExistingLines() {
    Order order = sampleOrder();
    OrderEntity entity = OrderEntity.fromDomain(order);
    when(orderRepository.update(any(OrderEntity.class))).thenReturn(entity);

    adapter.update(order);

    verify(orderLineRepository).deleteByOrderId(order.id());
  }

  @Test
  void updateInsertsOrderLinesIntoRepository() {
    Order order = sampleOrder();
    OrderEntity entity = OrderEntity.fromDomain(order);
    when(orderRepository.update(any(OrderEntity.class))).thenReturn(entity);

    adapter.update(order);

    verify(orderLineRepository).insert(any(OrderLineEntity.class));
  }

  private static Order sampleOrder() {
    DeliveryAddress delivery = new DeliveryAddress(
        "John Doe", "123456789", "Main St 1", "Apt 2", "City", "State", "12345", "Country");
    InvoiceAddress invoice = new InvoiceAddress(
        "Company Inc", "VAT123", "987654321", "Market St 5", null, "City", "State",
        "12345", "Country");
    OrderLine line = new OrderLine(PROD_ID, 2, 2_500);
    return new Order(
        ID, USER_ID, "John Doe", "john@example.com", NOW, OrderStatus.PENDING, 5_000, "EUR",
        delivery, invoice, List.of(line), NOW, NOW);
  }
}
