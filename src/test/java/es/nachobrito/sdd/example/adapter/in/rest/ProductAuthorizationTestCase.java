package es.nachobrito.sdd.example.adapter.in.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@MicronautTest
class ProductAuthorizationTestCase {

  @Test
  void enforcesCatalogAndCustomerRolesOnProducts(RequestSpecification spec) {
    given(spec).when().get("/v1/products").then().statusCode(401);

    String catalogToken = login(spec, "catalog", "catalog1234");
    String customerToken = registerAndLoginCustomer(spec);
    assertNotNull(catalogToken);
    assertNotNull(customerToken);

    UUID activeId = UUID.randomUUID();
    UUID draftId = UUID.randomUUID();

    createProduct(spec, catalogToken, activeId, "ACTIVE", 201);
    createProduct(spec, catalogToken, draftId, "DRAFT", 201);
    createProduct(spec, customerToken, UUID.randomUUID(), "ACTIVE", 403);

    given(spec)
        .header("Authorization", "Bearer " + customerToken)
        .when()
        .get("/v1/products")
        .then()
        .statusCode(200)
        .body("id", hasItem(activeId.toString()))
        .body("id", not(hasItem(draftId.toString())));

    given(spec)
        .header("Authorization", "Bearer " + customerToken)
        .when()
        .get("/v1/products/" + activeId)
        .then()
        .statusCode(200);

    given(spec)
        .header("Authorization", "Bearer " + customerToken)
        .when()
        .get("/v1/products/" + draftId)
        .then()
        .statusCode(403);

    given(spec)
        .header("Authorization", "Bearer " + catalogToken)
        .when()
        .get("/v1/products")
        .then()
        .statusCode(200)
        .body("id", hasItem(activeId.toString()))
        .body("id", hasItem(draftId.toString()));

    given(spec)
        .header("Authorization", "Bearer " + catalogToken)
        .when()
        .get("/v1/products/" + draftId)
        .then()
        .statusCode(200);
  }

  private static void createProduct(
      RequestSpecification spec, String token, UUID id, String status, int expectedStatus) {
    String sku = "AUTH-" + id.toString().substring(0, 8);
    String payload =
        "{\"id\":\"" + id + "\",\"sku\":\"" + sku + "\",\"name\":\"Auth Product\","
            + "\"description\":\"Test\",\"priceInCents\":1000,\"currency\":\"EUR\",\"status\":\""
            + status + "\"}";
    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
        .body(payload)
        .when()
        .post("/v1/products")
        .then()
        .statusCode(expectedStatus);
  }

  private static String registerAndLoginCustomer(RequestSpecification spec) {
    UUID userId = UUID.randomUUID();
    String username = "cust_" + userId.toString().substring(0, 8);
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

