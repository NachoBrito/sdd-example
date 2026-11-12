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
    UUID productId = createProduct(spec);
    UUID orderId = UUID.randomUUID();
    String payload = createOrderPayload(orderId, productId, "PENDING", "alice@example.com");

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
    UUID productId = createProduct(spec);
    UUID orderId = UUID.randomUUID();
    String payload = createOrderPayload(orderId, productId, "PENDING", "bob@example.com");

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
    UUID productId = createProduct(spec);
    UUID orderId = UUID.randomUUID();
    String payload = createOrderPayload(orderId, productId, "PENDING", "charlie@example.com");

    given(spec)
        .contentType(ContentType.JSON)
        .body(payload)
        .when()
        .post("/v1/orders")
        .then()
        .statusCode(201);

    String updatePayload = updateOrderPayload(productId, "PAID", "charlie@example.com");

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
    UUID productId = createProduct(spec);
    UUID orderId = UUID.randomUUID();
    String payload = createOrderPayload(orderId, productId, "PENDING", "dana@example.com");

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
    UUID productId = createProduct(spec);
    UUID orderId = UUID.randomUUID();
    String payload = createOrderPayload(orderId, productId, "PAID", "elena@example.com");

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
    UUID productId = createProduct(spec);
    UUID orderId = UUID.randomUUID();
    String payload = createOrderPayload(orderId, productId, "CANCELLED", "frank@example.com");

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

  private static UUID createProduct(RequestSpecification spec) {
    UUID productId = UUID.randomUUID();
    String skuSuffix = productId.toString().substring(0, 8);
    String createPayload = "{"
        + "\"id\":\"" + productId + "\","
        + "\"sku\":\"PROD-" + skuSuffix + "\","
        + "\"name\":\"Sample Product\","
        + "\"description\":\"Sample product description\","
        + "\"priceInCents\":1500,"
        + "\"currency\":\"EUR\","
        + "\"status\":\"ACTIVE\""
        + "}";

    given(spec)
        .contentType(ContentType.JSON)
        .body(createPayload)
        .when()
        .post("/v1/products")
        .then()
        .statusCode(201);

    return productId;
  }

  private static String createOrderPayload(
      UUID orderId, UUID productId, String status, String email) {
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

  private static String updateOrderPayload(UUID productId, String status, String email) {
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

