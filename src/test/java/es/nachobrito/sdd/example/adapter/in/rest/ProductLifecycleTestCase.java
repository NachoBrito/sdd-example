package es.nachobrito.sdd.example.adapter.in.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@MicronautTest
class ProductLifecycleTestCase {

  @Test
  void createProductReturnsCreatedStatus(RequestSpecification spec) {
    String token = loginAsCatalog(spec);
    UUID productId = UUID.randomUUID();
    String skuSuffix = productId.toString().substring(0, 8);
    String createPayload = "{"
        + "\"id\":\"" + productId + "\","
        + "\"sku\":\"KEYBOARD-" + skuSuffix + "\","
        + "\"name\":\"Mechanical Keyboard\","
        + "\"description\":\"Tactile switch keyboard\","
        + "\"priceInCents\":12900,"
        + "\"currency\":\"USD\","
        + "\"status\":\"DRAFT\""
        + "}";

    int createStatus = given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
        .body(createPayload)
        .when()
        .post("/v1/products")
        .then()
        .extract()
        .statusCode();
    assertEquals(201, createStatus);
  }

  @Test
  void retrieveProductReturnsOkStatus(RequestSpecification spec) {
    String token = loginAsCatalog(spec);
    UUID productId = UUID.randomUUID();
    String skuSuffix = productId.toString().substring(0, 8);
    String createPayload = "{"
        + "\"id\":\"" + productId + "\","
        + "\"sku\":\"KEYBOARD-" + skuSuffix + "\","
        + "\"name\":\"Mechanical Keyboard\","
        + "\"priceInCents\":12900,"
        + "\"currency\":\"USD\","
        + "\"status\":\"DRAFT\""
        + "}";

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
        .body(createPayload)
        .when()
        .post("/v1/products")
        .then()
        .statusCode(201);

    int getStatus = given(spec)
        .header("Authorization", "Bearer " + token)
        .when()
        .get("/v1/products/" + productId)
        .then()
        .extract()
        .statusCode();
    assertEquals(200, getStatus);
  }

  @Test
  void updateProductReturnsOkStatus(RequestSpecification spec) {
    String token = loginAsCatalog(spec);
    UUID productId = UUID.randomUUID();
    String skuSuffix = productId.toString().substring(0, 8);
    String createPayload = "{"
        + "\"id\":\"" + productId + "\","
        + "\"sku\":\"KEYBOARD-" + skuSuffix + "\","
        + "\"name\":\"Mechanical Keyboard\","
        + "\"priceInCents\":12900,"
        + "\"currency\":\"USD\","
        + "\"status\":\"DRAFT\""
        + "}";

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
        .body(createPayload)
        .when()
        .post("/v1/products")
        .then()
        .statusCode(201);

    String updatePayload = "{"
        + "\"sku\":\"KEYBOARD-" + skuSuffix + "\","
        + "\"name\":\"Mechanical Keyboard Pro\","
        + "\"priceInCents\":14900,"
        + "\"currency\":\"USD\","
        + "\"status\":\"ACTIVE\""
        + "}";

    int updateStatus = given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
        .body(updatePayload)
        .when()
        .put("/v1/products/" + productId)
        .then()
        .extract()
        .statusCode();
    assertEquals(200, updateStatus);
  }

  @Test
  void deleteProductReturnsNoContentStatus(RequestSpecification spec) {
    String token = loginAsCatalog(spec);
    UUID productId = UUID.randomUUID();
    String skuSuffix = productId.toString().substring(0, 8);
    String createPayload = "{"
        + "\"id\":\"" + productId + "\","
        + "\"sku\":\"KEYBOARD-" + skuSuffix + "\","
        + "\"name\":\"Mechanical Keyboard\","
        + "\"priceInCents\":12900,"
        + "\"currency\":\"USD\","
        + "\"status\":\"DRAFT\""
        + "}";

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
        .body(createPayload)
        .when()
        .post("/v1/products")
        .then()
        .statusCode(201);

    int deleteStatus = given(spec)
        .header("Authorization", "Bearer " + token)
        .when()
        .delete("/v1/products/" + productId)
        .then()
        .extract()
        .statusCode();
    assertEquals(204, deleteStatus);
  }

  @Test
  void getDeletedProductReturnsNotFoundStatus(RequestSpecification spec) {
    String token = loginAsCatalog(spec);
    UUID productId = UUID.randomUUID();
    String skuSuffix = productId.toString().substring(0, 8);
    String createPayload = "{"
        + "\"id\":\"" + productId + "\","
        + "\"sku\":\"KEYBOARD-" + skuSuffix + "\","
        + "\"name\":\"Mechanical Keyboard\","
        + "\"priceInCents\":12900,"
        + "\"currency\":\"USD\","
        + "\"status\":\"DRAFT\""
        + "}";

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
        .body(createPayload)
        .when()
        .post("/v1/products")
        .then()
        .statusCode(201);

    given(spec)
        .header("Authorization", "Bearer " + token)
        .when()
        .delete("/v1/products/" + productId)
        .then()
        .statusCode(204);

    int verifyStatus = given(spec)
        .header("Authorization", "Bearer " + token)
        .when()
        .get("/v1/products/" + productId)
        .then()
        .extract()
        .statusCode();
    assertEquals(404, verifyStatus);
  }

  private static String loginAsCatalog(RequestSpecification spec) {
    return given(spec)
        .contentType(ContentType.JSON)
        .body("{\"username\":\"catalog\",\"password\":\"catalog1234\"}")
        .when()
        .post("/v1/auth/login")
        .then()
        .statusCode(200)
        .extract()
        .path("accessToken");
  }
}
