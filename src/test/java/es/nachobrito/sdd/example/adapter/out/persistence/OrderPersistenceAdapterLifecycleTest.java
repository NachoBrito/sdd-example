package es.nachobrito.sdd.example.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderPersistenceAdapterLifecycleTest {

  @Mock
  private OrderJdbcRepository orderRepository;

  @Mock
  private OrderLineJdbcRepository orderLineRepository;

  private OrderPersistenceAdapter adapter;

  private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @BeforeEach
  void setUp() {
    adapter = new OrderPersistenceAdapter(orderRepository, orderLineRepository);
  }

  @Test
  void deleteByIdDeletesOrderLines() {
    adapter.deleteById(ID);

    verify(orderLineRepository).deleteByOrderId(ID);
  }

  @Test
  void deleteByIdDeletesOrder() {
    adapter.deleteById(ID);

    verify(orderRepository).deleteById(ID);
  }

  @Test
  void saveThrowsWhenOrderIsNull() {
    assertThrows(NullPointerException.class, () -> adapter.save(null));
  }

  @Test
  void updateThrowsWhenOrderIsNull() {
    assertThrows(NullPointerException.class, () -> adapter.update(null));
  }

  @Test
  void deleteByIdThrowsWhenIdIsNull() {
    assertThrows(NullPointerException.class, () -> adapter.deleteById(null));
  }
}

