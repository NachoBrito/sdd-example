package es.nachobrito.sdd.example.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.nachobrito.sdd.example.domain.model.OrderLine;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderLineEntityTest {

  private static final UUID LINE_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  private static final UUID ORDER_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
  private static final UUID PROD_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

  @Test
  void fromDomainMapsAllFieldsCorrectly() {
    OrderLine line = new OrderLine(PROD_ID, 3, 1_500);
    OrderLineEntity entity = OrderLineEntity.fromDomain(LINE_ID, ORDER_ID, line);

    assertEquals(4_500, entity.lineTotalInCents());
  }

  @Test
  void toDomainMapsAllFieldsCorrectly() {
    OrderLineEntity entity = new OrderLineEntity(LINE_ID, ORDER_ID, PROD_ID, 3, 1_500, 4_500);
    OrderLine line = entity.toDomain();

    assertEquals(4_500, line.lineTotalInCents());
  }
}

