package es.nachobrito.sdd.example.adapter.in.rest.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.nachobrito.sdd.example.domain.model.DeliveryAddress;
import es.nachobrito.sdd.example.domain.model.InvoiceAddress;
import es.nachobrito.sdd.example.domain.model.OrderLine;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AddressAndLineDtoTest {

  private static final UUID PROD_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  @Test
  void deliveryAddressDtoMapsToAndFromDomain() {
    DeliveryAddress domain = new DeliveryAddress(
        "John Doe", "123456789", "Main St 1", "Apt 2", "City", "State", "12345", "Country");
    DeliveryAddressDto dto = DeliveryAddressDto.fromDomain(domain);

    assertEquals(domain, dto.toDomain());
  }

  @Test
  void invoiceAddressDtoMapsToAndFromDomain() {
    InvoiceAddress domain = new InvoiceAddress(
        "Company Inc", "VAT123", "987654321", "Market St 5", null, "City", "State",
        "12345", "Country");
    InvoiceAddressDto dto = InvoiceAddressDto.fromDomain(domain);

    assertEquals(domain, dto.toDomain());
  }

  @Test
  void orderLineRequestMapsToDomain() {
    OrderLineRequest request = new OrderLineRequest(PROD_ID, 3, 500, null);
    OrderLine domain = request.toDomain();

    assertEquals(1_500, domain.lineTotalInCents());
  }

  @Test
  void orderLineResponseMapsFromDomain() {
    OrderLine domain = new OrderLine(PROD_ID, 3, 500);
    OrderLineResponse response = OrderLineResponse.fromDomain(domain);

    assertEquals(1_500, response.lineTotalInCents());
  }
}

