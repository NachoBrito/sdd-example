package es.nachobrito.sdd.example.adapter.in.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@MicronautTest
class CampaignLifecycleTestCase {

  @Test
  void supportsAdminCampaignCrudFilteringValidationAndAuthorization(
      RequestSpecification spec) {
    UUID campaignId = UUID.randomUUID();
    given(spec).when().get("/v1/campaigns").then().statusCode(401);
    given(spec)
        .contentType(ContentType.JSON)
        .body("{\"id\":\"" + campaignId + "\"}")
        .when()
        .post("/v1/campaigns")
        .then()
        .statusCode(401);

    String catalogToken = login(spec, "catalog", "catalog1234");
    assertNotNull(catalogToken);
    given(spec)
        .header("Authorization", "Bearer " + catalogToken)
        .when()
        .get("/v1/campaigns")
        .then()
        .statusCode(403);

    String customerToken = registerAndLoginCustomer(spec);
    assertNotNull(customerToken);
    given(spec)
        .header("Authorization", "Bearer " + customerToken)
        .when()
        .get("/v1/campaigns")
        .then()
        .statusCode(403);

    String adminToken = login(spec, "admin", "admin1234");
    assertNotNull(adminToken);
    String suffix = campaignId.toString().substring(0, 8);
    String validPayload =
        "{\"id\":\"" + campaignId + "\",\"name\":\"Autumn-" + suffix + "\","
            + "\"description\":\"Seasonal audio markdown\",\"status\":\"DRAFT\","
            + "\"startDate\":\"2026-11-01T00:00:00Z\",\"endDate\":\"2026-11-30T23:59:59Z\","
            + "\"discountPercent\":20,\"predicates\":["
            + "{\"predicateType\":\"IS_CHEAPER_THAN\",\"value\":\"10000\"}]}";

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + catalogToken)
        .body(validPayload)
        .when()
        .post("/v1/campaigns")
        .then()
        .statusCode(403);

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + adminToken)
        .body(validPayload)
        .when()
        .post("/v1/campaigns")
        .then()
        .statusCode(201)
        .header("Location", "/v1/campaigns/" + campaignId)
        .body("status", equalTo("DRAFT"))
        .body("predicates", hasSize(1));

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + adminToken)
        .body(validPayload)
        .when()
        .post("/v1/campaigns")
        .then()
        .statusCode(409);

    String invalidRangePayload =
        "{\"id\":\"" + UUID.randomUUID() + "\",\"name\":\"Invalid Window\","
            + "\"status\":\"ACTIVE\",\"startDate\":\"2026-12-10T00:00:00Z\","
            + "\"endDate\":\"2026-12-01T00:00:00Z\",\"discountPercent\":10,\"predicates\":[]}";
    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + adminToken)
        .body(invalidRangePayload)
        .when()
        .post("/v1/campaigns")
        .then()
        .statusCode(400);

    String nullElementPayload =
        "{\"id\":\"" + UUID.randomUUID() + "\",\"name\":\"Null Predicate\","
            + "\"status\":\"ACTIVE\",\"startDate\":\"2026-11-01T00:00:00Z\","
            + "\"endDate\":\"2026-11-30T00:00:00Z\",\"discountPercent\":10,\"predicates\":[null]}";
    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + adminToken)
        .body(nullElementPayload)
        .when()
        .post("/v1/campaigns")
        .then()
        .statusCode(400);

    String updatePayload =
        "{\"name\":\"Winter-" + suffix + "\",\"description\":\"Clearance sale\","
            + "\"status\":\"ACTIVE\",\"startDate\":\"2026-11-10T00:00:00Z\","
            + "\"endDate\":\"2026-11-25T00:00:00Z\",\"discountPercent\":35,"
            + "\"predicates\":["
            + "{\"predicateType\":\"IS_MORE_EXPENSIVE_THAN\",\"value\":\"500\"},"
            + "{\"predicateType\":\"IS_CHEAPER_THAN\",\"value\":\"9000\"}]}";
    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + adminToken)
        .body(updatePayload)
        .when()
        .put("/v1/campaigns/" + campaignId)
        .then()
        .statusCode(200)
        .body("name", equalTo("Winter-" + suffix))
        .body("status", equalTo("ACTIVE"))
        .body("discountPercent", equalTo(35))
        .body("predicates", hasSize(2));

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + adminToken)
        .body(updatePayload)
        .when()
        .put("/v1/campaigns/" + UUID.randomUUID())
        .then()
        .statusCode(404);

    given(spec)
        .header("Authorization", "Bearer " + adminToken)
        .queryParam("name", "winter-" + suffix)
        .queryParam("description", "clearance")
        .queryParam("status", "ACTIVE")
        .queryParam("startDate", "2026-11-15T00:00:00Z")
        .queryParam("endDate", "2026-11-20T00:00:00Z")
        .when()
        .get("/v1/campaigns")
        .then()
        .statusCode(200)
        .body("id", hasItem(campaignId.toString()));

    given(spec)
        .header("Authorization", "Bearer " + adminToken)
        .queryParam("startDate", "2027-01-01T00:00:00Z")
        .queryParam("endDate", "2027-01-10T00:00:00Z")
        .when()
        .get("/v1/campaigns")
        .then()
        .statusCode(200)
        .body("id", not(hasItem(campaignId.toString())));

    given(spec)
        .header("Authorization", "Bearer " + adminToken)
        .queryParam("startDate", "2026-12-01T00:00:00Z")
        .queryParam("endDate", "2026-11-01T00:00:00Z")
        .when()
        .get("/v1/campaigns")
        .then()
        .statusCode(400);

    given(spec)
        .header("Authorization", "Bearer " + catalogToken)
        .when()
        .delete("/v1/campaigns/" + campaignId)
        .then()
        .statusCode(403);

    given(spec)
        .header("Authorization", "Bearer " + adminToken)
        .when()
        .delete("/v1/campaigns/" + campaignId)
        .then()
        .statusCode(204);

    given(spec)
        .header("Authorization", "Bearer " + adminToken)
        .when()
        .delete("/v1/campaigns/" + campaignId)
        .then()
        .statusCode(404);
  }

  private static String registerAndLoginCustomer(RequestSpecification spec) {
    UUID userId = UUID.randomUUID();
    String username = "campcust_" + userId.toString().substring(0, 8);
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

