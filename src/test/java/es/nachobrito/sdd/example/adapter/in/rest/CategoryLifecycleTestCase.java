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
class CategoryLifecycleTestCase {

  @Test
  void enforcesAuthorizationAndSupportsCategoryLifecycleAndInUseConflict(
      RequestSpecification spec) {
    UUID audioId = UUID.randomUUID();
    given(spec).when().get("/v1/categories").then().statusCode(401);
    given(spec)
        .contentType(ContentType.JSON)
        .body("{\"id\":\"" + audioId + "\",\"name\":\"Unauth\"}")
        .when()
        .post("/v1/categories")
        .then()
        .statusCode(401);
    given(spec).when().delete("/v1/categories/" + audioId).then().statusCode(401);

    String adminToken = login(spec, "admin", "admin1234");
    String catalogToken = login(spec, "catalog", "catalog1234");
    String customerToken = registerAndLoginCustomer(spec);
    assertNotNull(adminToken);

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + catalogToken)
        .body("{\"id\":\"" + audioId + "\",\"name\":\"Forbidden\"}")
        .when()
        .post("/v1/categories")
        .then()
        .statusCode(403);

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + customerToken)
        .body("{\"id\":\"" + audioId + "\",\"name\":\"Forbidden\"}")
        .when()
        .post("/v1/categories")
        .then()
        .statusCode(403);

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + adminToken)
        .body("{\"id\":\"" + audioId + "\",\"name\":\"\"}")
        .when()
        .post("/v1/categories")
        .then()
        .statusCode(400);

    String suffix = audioId.toString().substring(0, 8);
    String audioName = "Audio-" + suffix;
    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + adminToken)
        .body("{\"id\":\"" + audioId + "\",\"name\":\"" + audioName
            + "\",\"description\":\"HiFi Headphones " + suffix + "\"}")
        .when()
        .post("/v1/categories")
        .then()
        .statusCode(201)
        .header("Location", "/v1/categories/" + audioId)
        .body("name", equalTo(audioName));

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + adminToken)
        .body("{\"id\":\"" + audioId + "\",\"name\":\"" + audioName + "\"}")
        .when()
        .post("/v1/categories")
        .then()
        .statusCode(409);

    UUID tempId = UUID.randomUUID();
    String tempName = "Temp-" + tempId.toString().substring(0, 8);
    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + adminToken)
        .body("{\"id\":\"" + tempId + "\",\"name\":\"" + tempName
            + "\",\"description\":\"Temporary shelf\"}")
        .when()
        .post("/v1/categories")
        .then()
        .statusCode(201);

    given(spec)
        .header("Authorization", "Bearer " + customerToken)
        .queryParam("name", "audio-" + suffix)
        .queryParam("description", "headphones")
        .when()
        .get("/v1/categories")
        .then()
        .statusCode(200)
        .body("id", hasItem(audioId.toString()))
        .body("id", not(hasItem(tempId.toString())));

    given(spec)
        .header("Authorization", "Bearer " + catalogToken)
        .when()
        .get("/v1/categories")
        .then()
        .statusCode(200)
        .body("id", hasItem(audioId.toString()))
        .body("id", hasItem(tempId.toString()));

    given(spec)
        .header("Authorization", "Bearer " + customerToken)
        .when()
        .delete("/v1/categories/" + tempId)
        .then()
        .statusCode(403);

    given(spec)
        .header("Authorization", "Bearer " + adminToken)
        .when()
        .delete("/v1/categories/" + tempId)
        .then()
        .statusCode(204);

    given(spec)
        .header("Authorization", "Bearer " + adminToken)
        .when()
        .delete("/v1/categories/" + tempId)
        .then()
        .statusCode(404);

    UUID prodId = UUID.randomUUID();
    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + catalogToken)
        .body("{\"id\":\"" + prodId + "\",\"sku\":\"CAT-" + suffix + "\",\"name\":\"Headphone\","
            + "\"priceInCents\":5000,\"currency\":\"EUR\",\"status\":\"ACTIVE\","
            + "\"categoryId\":\"" + audioId + "\"}")
        .when()
        .post("/v1/products")
        .then()
        .statusCode(201);

    given(spec)
        .header("Authorization", "Bearer " + adminToken)
        .when()
        .delete("/v1/categories/" + audioId)
        .then()
        .statusCode(409);

    given(spec)
        .header("Authorization", "Bearer " + catalogToken)
        .when()
        .delete("/v1/products/" + prodId)
        .then()
        .statusCode(204);

    given(spec)
        .header("Authorization", "Bearer " + adminToken)
        .when()
        .delete("/v1/categories/" + audioId)
        .then()
        .statusCode(204);
  }

  private static String registerAndLoginCustomer(RequestSpecification spec) {
    UUID userId = UUID.randomUUID();
    String username = "catcust_" + userId.toString().substring(0, 8);
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
