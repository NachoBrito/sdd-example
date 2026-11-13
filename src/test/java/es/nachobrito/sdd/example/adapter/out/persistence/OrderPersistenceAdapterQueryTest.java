package es.nachobrito.sdd.example.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.domain.model.DeliveryAddress;
import es.nachobrito.sdd.example.domain.model.InvoiceAddress;
import es.nachobrito.sdd.example.domain.model.Order;
import es.nachobrito.sdd.example.domain.model.OrderLine;
import es.nachobrito.sdd.example.domain.model.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderPersistenceAdapterQueryTest {

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
  void findByIdReturnsMappedOrderWhenFound() {
    Order order = sampleOrder();
    OrderEntity entity = OrderEntity.fromDomain(order);
    OrderLineEntity lineEntity = OrderLineEntity.fromDomain(
        UUID.randomUUID(), ID, order.orderLines().getFirst());
    when(orderRepository.findById(ID)).thenReturn(Optional.of(entity));
    when(orderLineRepository.findByOrderId(ID)).thenReturn(List.of(lineEntity));

    Optional<Order> found = adapter.findById(ID);

    assertEquals(Optional.of(order), found);
  }

  @Test
  void findByIdReturnsEmptyWhenNotFound() {
    when(orderRepository.findById(ID)).thenReturn(Optional.empty());

    Optional<Order> found = adapter.findById(ID);

    assertEquals(Optional.empty(), found);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void existsByIdDelegatesToRepository(boolean exists) {
    when(orderRepository.existsById(ID)).thenReturn(exists);

    assertEquals(exists, adapter.existsById(ID));
  }

  @Test
  void findByIdThrowsWhenIdIsNull() {
    assertThrows(NullPointerException.class, () -> adapter.findById(null));
  }

  @Test
  void existsByIdThrowsWhenIdIsNull() {
    assertThrows(NullPointerException.class, () -> adapter.existsById(null));
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
