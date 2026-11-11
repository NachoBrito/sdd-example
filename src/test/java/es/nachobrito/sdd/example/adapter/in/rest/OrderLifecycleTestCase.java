package es.nachobrito.sdd.example.adapter.in.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@MicronautTest
class OrderLifecycleTestCase {

  @Test
  void createOrderReturnsCreatedStatus(RequestSpecification spec) {
    UUID orderId = UUID.randomUUID();
    String payload = createOrderPayload(orderId, "PENDING", "alice@example.com");

    int status = given(spec)
        .contentType(ContentType.JSON)
        .body(payload)
        .when()
        .post("/v1/orders")
        .then()
        .extract()
        .statusCode();

    assertEquals(201, status);
  }

  @Test
  void retrieveOrderReturnsOkStatus(RequestSpecification spec) {
    UUID orderId = UUID.randomUUID();
    String payload = createOrderPayload(orderId, "PENDING", "bob@example.com");

    given(spec)
        .contentType(ContentType.JSON)
        .body(payload)
        .when()
        .post("/v1/orders")
        .then()
        .statusCode(201);

    int status = given(spec)
        .when()
        .get("/v1/orders/" + orderId)
        .then()
        .extract()
        .statusCode();

    assertEquals(200, status);
  }

  @Test
  void updateOrderReturnsOkStatus(RequestSpecification spec) {
    UUID orderId = UUID.randomUUID();
    String payload = createOrderPayload(orderId, "PENDING", "charlie@example.com");

    given(spec)
        .contentType(ContentType.JSON)
        .body(payload)
        .when()
        .post("/v1/orders")
        .then()
        .statusCode(201);

    String updatePayload = updateOrderPayload("PAID", "charlie@example.com");

    int status = given(spec)
        .contentType(ContentType.JSON)
        .body(updatePayload)
        .when()
        .put("/v1/orders/" + orderId)
        .then()
        .extract()
        .statusCode();

    assertEquals(200, status);
  }

  @Test
  void deletePendingOrderReturnsNoContentStatus(RequestSpecification spec) {
    UUID orderId = UUID.randomUUID();
    String payload = createOrderPayload(orderId, "PENDING", "dana@example.com");

    given(spec)
        .contentType(ContentType.JSON)
        .body(payload)
        .when()
        .post("/v1/orders")
        .then()
        .statusCode(201);

    int status = given(spec)
        .when()
        .delete("/v1/orders/" + orderId)
        .then()
        .extract()
        .statusCode();

    assertEquals(204, status);
  }

  @Test
  void deletePaidOrderReturnsConflictStatus(RequestSpecification spec) {
    UUID orderId = UUID.randomUUID();
    String payload = createOrderPayload(orderId, "PAID", "elena@example.com");

    given(spec)
        .contentType(ContentType.JSON)
        .body(payload)
        .when()
        .post("/v1/orders")
        .then()
        .statusCode(201);

    int status = given(spec)
        .when()
        .delete("/v1/orders/" + orderId)
        .then()
        .extract()
        .statusCode();

    assertEquals(409, status);
  }

  @Test
  void getDeletedOrderReturnsNotFoundStatus(RequestSpecification spec) {
    UUID orderId = UUID.randomUUID();
    String payload = createOrderPayload(orderId, "CANCELLED", "frank@example.com");

    given(spec)
        .contentType(ContentType.JSON)
        .body(payload)
        .when()
        .post("/v1/orders")
        .then()
        .statusCode(201);

    given(spec)
        .when()
        .delete("/v1/orders/" + orderId)
        .then()
        .statusCode(204);

    int status = given(spec)
        .when()
        .get("/v1/orders/" + orderId)
        .then()
        .extract()
        .statusCode();

    assertEquals(404, status);
  }

  private static String createOrderPayload(UUID orderId, String status, String email) {
    UUID productId = UUID.randomUUID();
    return "{"
        + "\"id\":\"" + orderId + "\","
        + "\"customerName\":\"Customer Name\","
        + "\"customerEmail\":\"" + email + "\","
        + "\"date\":\"2026-11-10T10:00:00Z\","
        + "\"status\":\"" + status + "\","
        + "\"currency\":\"EUR\","
        + "\"deliveryAddress\":{"
        + "\"name\":\"Recipient\","
        + "\"telephone\":\"+34123456789\","
        + "\"street\":\"Street 1\","
        + "\"city\":\"Madrid\","
        + "\"state\":\"Madrid\","
        + "\"zipCode\":\"28001\","
        + "\"country\":\"Spain\""
        + "},"
        + "\"invoiceAddress\":{"
        + "\"name\":\"Invoice Corp\","
        + "\"vatNumber\":\"ESB12345678\","
        + "\"telephone\":\"+34123456789\","
        + "\"street\":\"Street 2\","
        + "\"city\":\"Madrid\","
        + "\"state\":\"Madrid\","
        + "\"zipCode\":\"28001\","
        + "\"country\":\"Spain\""
        + "},"
        + "\"orderLines\":[{"
        + "\"productId\":\"" + productId + "\","
        + "\"quantity\":2,"
        + "\"unitPriceInCents\":1500"
        + "}]"
        + "}";
  }

  private static String updateOrderPayload(String status, String email) {
    UUID productId = UUID.randomUUID();
    return "{"
        + "\"customerName\":\"Updated Name\","
        + "\"customerEmail\":\"" + email + "\","
        + "\"date\":\"2026-11-10T11:00:00Z\","
        + "\"status\":\"" + status + "\","
        + "\"currency\":\"EUR\","
        + "\"deliveryAddress\":{"
        + "\"name\":\"Recipient 2\","
        + "\"telephone\":\"+34123456789\","
        + "\"street\":\"Street 3\","
        + "\"city\":\"Madrid\","
        + "\"state\":\"Madrid\","
        + "\"zipCode\":\"28002\","
        + "\"country\":\"Spain\""
        + "},"
        + "\"invoiceAddress\":{"
        + "\"name\":\"Invoice Corp 2\","
        + "\"vatNumber\":\"ESB87654321\","
        + "\"telephone\":\"+34123456789\","
        + "\"street\":\"Street 4\","
        + "\"city\":\"Madrid\","
        + "\"state\":\"Madrid\","
        + "\"zipCode\":\"28002\","
        + "\"country\":\"Spain\""
        + "},"
        + "\"orderLines\":[{"
        + "\"productId\":\"" + productId + "\","
        + "\"quantity\":5,"
        + "\"unitPriceInCents\":2000"
        + "}]"
        + "}";
  }
}

