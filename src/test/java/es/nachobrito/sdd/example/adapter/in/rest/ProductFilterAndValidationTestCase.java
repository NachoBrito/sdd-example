package es.nachobrito.sdd.example.adapter.in.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@MicronautTest
class ProductFilterAndValidationTestCase {

  @Test
  void rejectDuplicateIdentifierReturnsConflict(RequestSpecification spec) {
    UUID productId = UUID.randomUUID();
    String suffix = productId.toString().substring(0, 8);
    String payload = "{"
        + "\"id\":\"" + productId + "\","
        + "\"sku\":\"UNIQUE-1-" + suffix + "\","
        + "\"name\":\"Product 1\","
        + "\"priceInCents\":1000,"
        + "\"currency\":\"EUR\","
        + "\"status\":\"ACTIVE\""
        + "}";

    given(spec)
        .contentType(ContentType.JSON)
        .body(payload)
        .when()
        .post("/v1/products")
        .then()
        .statusCode(201);

    String duplicatePayload = "{"
        + "\"id\":\"" + productId + "\","
        + "\"sku\":\"UNIQUE-2-" + suffix + "\","
        + "\"name\":\"Product 2\","
        + "\"priceInCents\":2000,"
        + "\"currency\":\"EUR\","
        + "\"status\":\"ACTIVE\""
        + "}";

    int duplicateStatus = given(spec)
        .contentType(ContentType.JSON)
        .body(duplicatePayload)
        .when()
        .post("/v1/products")
        .then()
        .extract()
        .statusCode();
    assertEquals(409, duplicateStatus);
  }

  @Test
  void rejectDuplicateSkuReturnsConflict(RequestSpecification spec) {
    String commonSku = "DUPLICATE-SKU-" + UUID.randomUUID().toString().substring(0, 8);
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    String payload1 = "{"
        + "\"id\":\"" + id1 + "\","
        + "\"sku\":\"" + commonSku + "\","
        + "\"name\":\"Product A\","
        + "\"priceInCents\":1000,"
        + "\"currency\":\"USD\","
        + "\"status\":\"DRAFT\""
        + "}";

    given(spec)
        .contentType(ContentType.JSON)
        .body(payload1)
        .when()
        .post("/v1/products")
        .then()
        .statusCode(201);

    String payload2 = "{"
        + "\"id\":\"" + id2 + "\","
        + "\"sku\":\"" + commonSku + "\","
        + "\"name\":\"Product B\","
        + "\"priceInCents\":2000,"
        + "\"currency\":\"USD\","
        + "\"status\":\"DRAFT\""
        + "}";

    int secondStatus = given(spec)
        .contentType(ContentType.JSON)
        .body(payload2)
        .when()
        .post("/v1/products")
        .then()
        .extract()
        .statusCode();
    assertEquals(409, secondStatus);
  }

  @Test
  void rejectNegativePriceReturnsBadRequest(RequestSpecification spec) {
    UUID id = UUID.randomUUID();
    String suffix = id.toString().substring(0, 8);
    String payload = "{"
        + "\"id\":\"" + id + "\","
        + "\"sku\":\"NEG-PRICE-" + suffix + "\","
        + "\"name\":\"Negative Price Product\","
        + "\"priceInCents\":-100,"
        + "\"currency\":\"USD\","
        + "\"status\":\"DRAFT\""
        + "}";

    int status = given(spec)
        .contentType(ContentType.JSON)
        .body(payload)
        .when()
        .post("/v1/products")
        .then()
        .extract()
        .statusCode();
    assertEquals(400, status);
  }

  @Test
  void filterByStatusReturnsOk(RequestSpecification spec) {
    String prefix = UUID.randomUUID().toString().substring(0, 6);
    UUID id1 = UUID.randomUUID();

    String p1 = "{"
        + "\"id\":\"" + id1 + "\","
        + "\"sku\":\"" + prefix + "-MOUSE-A\","
        + "\"name\":\"Ergonomic Mouse A\","
        + "\"priceInCents\":5000,"
        + "\"currency\":\"USD\","
        + "\"status\":\"ACTIVE\""
        + "}";

    given(spec)
        .contentType(ContentType.JSON)
        .body(p1)
        .when()
        .post("/v1/products")
        .then()
        .statusCode(201);

    int filterStatus = given(spec)
        .queryParam("status", "ACTIVE")
        .queryParam("sku", prefix + "-MOUSE-A")
        .when()
        .get("/v1/products")
        .then()
        .extract()
        .statusCode();
    assertEquals(200, filterStatus);
  }

  @Test
  void filterByNameRegexReturnsOk(RequestSpecification spec) {
    String prefix = UUID.randomUUID().toString().substring(0, 6);
    UUID id2 = UUID.randomUUID();

    String p2 = "{"
        + "\"id\":\"" + id2 + "\","
        + "\"sku\":\"" + prefix + "-MOUSE-B\","
        + "\"name\":\"Wireless Mouse B\","
        + "\"priceInCents\":6000,"
        + "\"currency\":\"USD\","
        + "\"status\":\"DRAFT\""
        + "}";

    given(spec)
        .contentType(ContentType.JSON)
        .body(p2)
        .when()
        .post("/v1/products")
        .then()
        .statusCode(201);

    int regexStatus = given(spec)
        .queryParam("nameRegex", ".*Mouse.*")
        .queryParam("sku", prefix + "-MOUSE-B")
        .when()
        .get("/v1/products")
        .then()
        .extract()
        .statusCode();
    assertEquals(200, regexStatus);
  }
}
