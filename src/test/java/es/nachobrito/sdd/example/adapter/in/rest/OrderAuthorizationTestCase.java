package es.nachobrito.sdd.example.adapter.in.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@MicronautTest
class OrderAuthorizationTestCase {

  @Test
  void enforcesOrderOwnershipForCustomerAndFullAccessForAdmin(RequestSpecification spec) {
    String adminToken = login(spec, "admin", "admin1234");
    String customer1Token = registerAndLoginCustomer(spec);
    String customer2Token = registerAndLoginCustomer(spec);
    assertNotNull(adminToken);
    assertNotNull(customer1Token);
    assertNotNull(customer2Token);

    UUID productId = UUID.randomUUID();
    String productPayload =
        "{\"id\":\"" + productId + "\",\"sku\":\"ORD-" + productId.toString().substring(0, 8)
            + "\",\"name\":\"Orderable Item\",\"description\":\"Desc\","
            + "\"priceInCents\":2000,\"currency\":\"EUR\",\"status\":\"ACTIVE\"}";
    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + adminToken)
        .body(productPayload)
        .when()
        .post("/v1/products")
        .then()
        .statusCode(201);

    UUID order1Id = UUID.randomUUID();
    UUID order2Id = UUID.randomUUID();
    createOrder(spec, customer1Token, order1Id, productId);
    createOrder(spec, customer2Token, order2Id, productId);

    given(spec)
        .header("Authorization", "Bearer " + customer1Token)
        .when()
        .get("/v1/orders")
        .then()
        .statusCode(200)
        .body("id", hasItem(order1Id.toString()))
        .body("id", not(hasItem(order2Id.toString())));

    given(spec)
        .header("Authorization", "Bearer " + customer1Token)
        .when()
        .get("/v1/orders/" + order2Id)
        .then()
        .statusCode(403);

    given(spec)
        .header("Authorization", "Bearer " + adminToken)
        .when()
        .get("/v1/orders")
        .then()
        .statusCode(200)
        .body("id", hasItem(order1Id.toString()))
        .body("id", hasItem(order2Id.toString()));

    given(spec)
        .header("Authorization", "Bearer " + adminToken)
        .when()
        .get("/v1/orders/" + order1Id)
        .then()
        .statusCode(200)
        .body("id", equalTo(order1Id.toString()));

    String updatePayload = orderUpdatePayload(productId);
    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + customer1Token)
        .body(updatePayload)
        .when()
        .put("/v1/orders/" + order1Id)
        .then()
        .statusCode(403);

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + adminToken)
        .body(updatePayload)
        .when()
        .put("/v1/orders/" + order1Id)
        .then()
        .statusCode(200)
        .body("customerName", equalTo("Updated Name"));
  }

  private static void createOrder(
      RequestSpecification spec, String token, UUID orderId, UUID productId) {
    String payload =
        "{\"id\":\"" + orderId + "\",\"customerName\":\"Test Customer\","
            + "\"customerEmail\":\"test@example.com\",\"currency\":\"EUR\","
            + "\"deliveryAddress\":{\"name\":\"Test\",\"telephone\":\"123\",\"street\":\"St\","
            + "\"city\":\"Madrid\",\"state\":\"Madrid\",\"zipCode\":\"28001\",\"country\":\"ES\"},"
            + "\"invoiceAddress\":{\"name\":\"Test\",\"vatNumber\":\"ES123\","
            + "\"telephone\":\"123\",\"street\":\"St\",\"city\":\"Madrid\","
            + "\"state\":\"Madrid\",\"zipCode\":\"28001\",\"country\":\"ES\"},"
            + "\"orderLines\":[{\"productId\":\"" + productId
            + "\",\"quantity\":1,\"unitPriceInCents\":2000}]}";
    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
        .body(payload)
        .when()
        .post("/v1/orders")
        .then()
        .statusCode(201);
  }

  private static String orderUpdatePayload(UUID productId) {
    return "{\"customerName\":\"Updated Name\",\"customerEmail\":\"test@example.com\","
        + "\"date\":\"2026-11-13T10:00:00Z\",\"status\":\"PENDING\",\"currency\":\"EUR\","
        + "\"deliveryAddress\":{\"name\":\"Updated\",\"telephone\":\"123\",\"street\":\"St\","
        + "\"city\":\"Madrid\",\"state\":\"Madrid\",\"zipCode\":\"28001\",\"country\":\"ES\"},"
        + "\"invoiceAddress\":{\"name\":\"Updated\",\"vatNumber\":\"ES123\","
        + "\"telephone\":\"123\",\"street\":\"St\",\"city\":\"Madrid\","
        + "\"state\":\"Madrid\",\"zipCode\":\"28001\",\"country\":\"ES\"},"
        + "\"orderLines\":[{\"productId\":\"" + productId
        + "\",\"quantity\":2,\"unitPriceInCents\":2000}]}";
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

