package es.nachobrito.sdd.example.adapter.in.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@MicronautTest
class ProductV2LifecycleTestCase {

  @Test
  void supportsV1OptionalCategoryAndV2RequiredCategoryWithLookupValidation(
      RequestSpecification spec) {
    String adminToken = login(spec, "admin", "admin1234");
    String catalogToken = login(spec, "catalog", "catalog1234");

    UUID categoryId = UUID.randomUUID();
    String categoryName = "Peripherals-" + categoryId.toString().substring(0, 8);
    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + adminToken)
        .body("{\"id\":\"" + categoryId + "\",\"name\":\"" + categoryName
            + "\",\"description\":\"Keyboards and mice\"}")
        .when()
        .post("/v1/categories")
        .then()
        .statusCode(201);

    String discoveredId = given(spec)
        .header("Authorization", "Bearer " + catalogToken)
        .queryParam("name", categoryName)
        .when()
        .get("/v1/categories")
        .then()
        .statusCode(200)
        .extract()
        .path("[0].id");
    assertEquals(categoryId.toString(), discoveredId);

    UUID v1ProductId = UUID.randomUUID();
    String v1Sku = "V1-" + v1ProductId.toString().substring(0, 8);
    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + catalogToken)
        .body("{\"id\":\"" + v1ProductId + "\",\"sku\":\"" + v1Sku + "\",\"name\":\"Legacy Hub\","
            + "\"description\":\"No category\",\"priceInCents\":2500,"
            + "\"currency\":\"EUR\",\"status\":\"ACTIVE\"}")
        .when()
        .post("/v1/products")
        .then()
        .statusCode(201)
        .body("categoryId", nullValue());

    UUID unknownCategoryId = UUID.randomUUID();
    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + catalogToken)
        .body("{\"id\":\"" + UUID.randomUUID() + "\",\"sku\":\"BAD-V1-" + unknownCategoryId
            + "\",\"name\":\"Bad Cat\",\"priceInCents\":1000,\"currency\":\"EUR\","
            + "\"status\":\"ACTIVE\",\"categoryId\":\"" + unknownCategoryId + "\"}")
        .when()
        .post("/v1/products")
        .then()
        .statusCode(400);

    UUID v2ProductId = UUID.randomUUID();
    String v2Sku = "V2-" + v2ProductId.toString().substring(0, 8);
    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + catalogToken)
        .body("{\"id\":\"" + v2ProductId + "\",\"sku\":\"" + v2Sku + "\",\"name\":\"Pro Mouse\","
            + "\"priceInCents\":4500,\"currency\":\"EUR\",\"status\":\"ACTIVE\"}")
        .when()
        .post("/v2/products")
        .then()
        .statusCode(400);

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + catalogToken)
        .body("{\"id\":\"" + v2ProductId + "\",\"sku\":\"" + v2Sku + "\",\"name\":\"Pro Mouse\","
            + "\"priceInCents\":4500,\"currency\":\"EUR\",\"status\":\"ACTIVE\","
            + "\"categoryId\":\"" + unknownCategoryId + "\"}")
        .when()
        .post("/v2/products")
        .then()
        .statusCode(400);

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + catalogToken)
        .body("{\"id\":\"" + v2ProductId + "\",\"sku\":\"" + v2Sku + "\",\"name\":\"Pro Mouse\","
            + "\"description\":\"Ergonomic\",\"priceInCents\":4500,\"currency\":\"EUR\","
            + "\"status\":\"ACTIVE\",\"categoryId\":\"" + categoryId + "\"}")
        .when()
        .post("/v2/products")
        .then()
        .statusCode(201)
        .header("Location", "/v2/products/" + v2ProductId)
        .body("categoryId", equalTo(categoryId.toString()));

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + catalogToken)
        .body("{\"sku\":\"" + v2Sku + "\",\"name\":\"Pro Mouse V2\","
            + "\"priceInCents\":4900,\"currency\":\"EUR\",\"status\":\"ACTIVE\"}")
        .when()
        .put("/v2/products/" + v2ProductId)
        .then()
        .statusCode(400);

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + catalogToken)
        .body("{\"sku\":\"" + v2Sku + "\",\"name\":\"Pro Mouse V2\","
            + "\"priceInCents\":4900,\"currency\":\"EUR\",\"status\":\"ACTIVE\","
            + "\"categoryId\":\"" + categoryId + "\"}")
        .when()
        .put("/v2/products/" + v2ProductId)
        .then()
        .statusCode(200)
        .body("name", equalTo("Pro Mouse V2"))
        .body("categoryId", equalTo(categoryId.toString()));

    String customerToken = registerAndLoginCustomer(spec);
    given(spec)
        .header("Authorization", "Bearer " + customerToken)
        .when()
        .get("/v2/products/" + v2ProductId)
        .then()
        .statusCode(200)
        .body("categoryId", equalTo(categoryId.toString()));

    given(spec)
        .header("Authorization", "Bearer " + customerToken)
        .when()
        .get("/v2/products")
        .then()
        .statusCode(200)
        .body("id", hasItem(v2ProductId.toString()));

    given(spec)
        .header("Authorization", "Bearer " + adminToken)
        .when()
        .delete("/v1/categories/" + categoryId)
        .then()
        .statusCode(409);

    given(spec)
        .header("Authorization", "Bearer " + catalogToken)
        .when()
        .delete("/v2/products/" + v2ProductId)
        .then()
        .statusCode(204);

    given(spec)
        .header("Authorization", "Bearer " + adminToken)
        .when()
        .delete("/v1/categories/" + categoryId)
        .then()
        .statusCode(204);
  }

  private static String registerAndLoginCustomer(RequestSpecification spec) {
    UUID userId = UUID.randomUUID();
    String username = "v2cust_" + userId.toString().substring(0, 8);
    String password = "CustomerPass123!";
    given(spec)
        .contentType(ContentType.JSON)
        .body("{\"userId\":\"" + userId + "\",\"username\":\"" + username + "\",\"password\":\""
            + password + "\"}")
        .when()
        .post("/v1/auth/register")
        .then()
        .statusCode(201);
    return login(spec, username, password);
  }

  private static String login(RequestSpecification spec, String username, String password) {
    return given(spec)
        .contentType(ContentType.JSON)
        .body("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
        .when()
        .post("/v1/auth/login")
        .then()
        .statusCode(200)
        .extract()
        .path("accessToken");
  }
}
