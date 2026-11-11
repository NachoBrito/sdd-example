package es.nachobrito.sdd.example.adapter.in.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@MicronautTest
class OrderFilterAndValidationTestCase {

  private static final String ORDERS_PATH = "/v1/orders";

  @Test
  void rejectDuplicateIdentifierReturnsConflict(RequestSpecification spec) {
    UUID orderId = UUID.randomUUID();
    String payload = sampleOrderPayload(orderId, "user1@example.com", 2, 1000);

    given(spec)
        .contentType(ContentType.JSON)
        .body(payload)
        .when()
        .post(ORDERS_PATH)
        .then()
        .statusCode(201);

    int duplicateStatus = given(spec)
        .contentType(ContentType.JSON)
        .body(payload)
        .when()
        .post(ORDERS_PATH)
        .then()
        .extract()
        .statusCode();

    assertEquals(409, duplicateStatus);
  }

  @Test
  void rejectInvalidEmailReturnsBadRequest(RequestSpecification spec) {
    UUID orderId = UUID.randomUUID();
    String payload = sampleOrderPayload(orderId, "not-an-email", 1, 1000);

    int status = given(spec)
        .contentType(ContentType.JSON)
        .body(payload)
        .when()
        .post(ORDERS_PATH)
        .then()
        .extract()
        .statusCode();

    assertEquals(400, status);
  }

  @Test
  void rejectNegativePriceReturnsBadRequest(RequestSpecification spec) {
    UUID orderId = UUID.randomUUID();
    String payload = sampleOrderPayload(orderId, "valid@example.com", 1, -500);

    int status = given(spec)
        .contentType(ContentType.JSON)
        .body(payload)
        .when()
        .post(ORDERS_PATH)
        .then()
        .extract()
        .statusCode();

    assertEquals(400, status);
  }

  @Test
  void rejectZeroQuantityReturnsBadRequest(RequestSpecification spec) {
    UUID orderId = UUID.randomUUID();
    String payload = sampleOrderPayload(orderId, "valid@example.com", 0, 500);

    int status = given(spec)
        .contentType(ContentType.JSON)
        .body(payload)
        .when()
        .post(ORDERS_PATH)
        .then()
        .extract()
        .statusCode();

    assertEquals(400, status);
  }

  @Test
  void filterOrdersByStatusReturnsOk(RequestSpecification spec) {
    UUID orderId = UUID.randomUUID();
    String payload = sampleOrderPayload(orderId, "filter-status@example.com", 1, 1000);

    given(spec)
        .contentType(ContentType.JSON)
        .body(payload)
        .when()
        .post(ORDERS_PATH)
        .then()
        .statusCode(201);

    int status = given(spec)
        .queryParam("status", "PENDING")
        .when()
        .get(ORDERS_PATH)
        .then()
        .extract()
        .statusCode();

    assertEquals(200, status);
  }

  @Test
  void filterOrdersByEmailAndSortReturnsOk(RequestSpecification spec) {
    UUID orderId = UUID.randomUUID();
    String email = "sort-test-" + orderId.toString().substring(0, 8) + "@example.com";
    String payload = sampleOrderPayload(orderId, email, 1, 1000);

    given(spec)
        .contentType(ContentType.JSON)
        .body(payload)
        .when()
        .post(ORDERS_PATH)
        .then()
        .statusCode(201);

    int status = given(spec)
        .queryParam("customerEmail", email)
        .queryParam("sort", "date")
        .queryParam("direction", "desc")
        .when()
        .get(ORDERS_PATH)
        .then()
        .extract()
        .statusCode();

    assertEquals(200, status);
  }

  private static String sampleOrderPayload(
      UUID orderId, String email, int quantity, int unitPriceInCents) {
    UUID productId = UUID.randomUUID();
    return "{"
        + "\"id\":\"" + orderId + "\","
        + "\"customerName\":\"Customer\","
        + "\"customerEmail\":\"" + email + "\","
        + "\"date\":\"2026-11-10T10:00:00Z\","
        + "\"status\":\"PENDING\","
        + "\"currency\":\"EUR\","
        + "\"deliveryAddress\":{"
        + "\"name\":\"Recipient\","
        + "\"telephone\":\"+34123456789\","
        + "\"street\":\"Street 1\","
        + "\"city\":\"City\","
        + "\"state\":\"State\","
        + "\"zipCode\":\"12345\","
        + "\"country\":\"Country\""
        + "},"
        + "\"invoiceAddress\":{"
        + "\"name\":\"Invoice Corp\","
        + "\"vatNumber\":\"VAT123\","
        + "\"telephone\":\"+34123456789\","
        + "\"street\":\"Street 2\","
        + "\"city\":\"City\","
        + "\"state\":\"State\","
        + "\"zipCode\":\"12345\","
        + "\"country\":\"Country\""
        + "},"
        + "\"orderLines\":[{"
        + "\"productId\":\"" + productId + "\","
        + "\"quantity\":" + quantity + ","
        + "\"unitPriceInCents\":" + unitPriceInCents
        + "}]"
        + "}";
  }
}

