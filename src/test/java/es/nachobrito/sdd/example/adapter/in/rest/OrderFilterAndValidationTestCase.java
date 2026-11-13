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
    String token = loginAsAdmin(spec);
    UUID productId = createProduct(spec, token);
    UUID orderId = UUID.randomUUID();
    String payload = sampleOrderPayload(orderId, productId, "user1@example.com", 2, 1000);

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
        .body(payload)
        .when()
        .post(ORDERS_PATH)
        .then()
        .statusCode(201);

    int duplicateStatus = given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
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
    String token = loginAsAdmin(spec);
    UUID orderId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    String payload = sampleOrderPayload(orderId, productId, "not-an-email", 1, 1000);

    int status = given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
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
    String token = loginAsAdmin(spec);
    UUID orderId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    String payload = sampleOrderPayload(orderId, productId, "valid@example.com", 1, -500);

    int status = given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
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
    String token = loginAsAdmin(spec);
    UUID orderId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    String payload = sampleOrderPayload(orderId, productId, "valid@example.com", 0, 500);

    int status = given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
        .body(payload)
        .when()
        .post(ORDERS_PATH)
        .then()
        .extract()
        .statusCode();

    assertEquals(400, status);
  }

  @Test
  void rejectUnexistingProductReturnsBadRequest(RequestSpecification spec) {
    String token = loginAsAdmin(spec);
    UUID orderId = UUID.randomUUID();
    UUID unexistingProductId = UUID.randomUUID();
    String payload =
        sampleOrderPayload(orderId, unexistingProductId, "valid@example.com", 1, 1000);

    int status = given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
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
    String token = loginAsAdmin(spec);
    UUID productId = createProduct(spec, token);
    UUID orderId = UUID.randomUUID();
    String payload = sampleOrderPayload(orderId, productId, "filter-status@example.com", 1, 1000);

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
        .body(payload)
        .when()
        .post(ORDERS_PATH)
        .then()
        .statusCode(201);

    int status = given(spec)
        .header("Authorization", "Bearer " + token)
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
    String token = loginAsAdmin(spec);
    UUID productId = createProduct(spec, token);
    UUID orderId = UUID.randomUUID();
    String email = "sort-test-" + orderId.toString().substring(0, 8) + "@example.com";
    String payload = sampleOrderPayload(orderId, productId, email, 1, 1000);

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
        .body(payload)
        .when()
        .post(ORDERS_PATH)
        .then()
        .statusCode(201);

    int status = given(spec)
        .header("Authorization", "Bearer " + token)
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

  private static String loginAsAdmin(RequestSpecification spec) {
    return given(spec)
        .contentType(ContentType.JSON)
        .body("{\"username\":\"admin\",\"password\":\"admin1234\"}")
        .when()
        .post("/v1/auth/login")
        .then()
        .statusCode(200)
        .extract()
        .path("accessToken");
  }

  private static UUID createProduct(RequestSpecification spec, String token) {
    UUID productId = UUID.randomUUID();
    String skuSuffix = productId.toString().substring(0, 8);
    String createPayload = "{"
        + "\"id\":\"" + productId + "\","
        + "\"sku\":\"PROD-" + skuSuffix + "\","
        + "\"name\":\"Sample Product\","
        + "\"description\":\"Sample product description\","
        + "\"priceInCents\":1000,"
        + "\"currency\":\"EUR\","
        + "\"status\":\"ACTIVE\""
        + "}";

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
        .body(createPayload)
        .when()
        .post("/v1/products")
        .then()
        .statusCode(201);

    return productId;
  }

  private static String sampleOrderPayload(
      UUID orderId, UUID productId, String email, int quantity, int unitPriceInCents) {
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

