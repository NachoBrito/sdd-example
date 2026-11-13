package es.nachobrito.sdd.example.adapter.in.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@MicronautTest
class AuthFlowTestCase {

  @Test
  void registerLoginValidateRefreshAndRevokeFlow(RequestSpecification spec) {
    UUID userId = UUID.randomUUID();
    String username = "user_" + userId.toString().substring(0, 8);
    String password = "StrongPassword123!";

    String registerPayload =
        "{\"userId\":\"" + userId + "\",\"username\":\"" + username + "\",\"password\":\""
            + password + "\"}";

    given(spec)
        .contentType(ContentType.JSON)
        .body(registerPayload)
        .when()
        .post("/v1/auth/register")
        .then()
        .statusCode(201)
        .body("userId", equalTo(userId.toString()))
        .body("username", equalTo(username))
        .body("roles", hasItem("CUSTOMER"));

    given(spec)
        .contentType(ContentType.JSON)
        .body(registerPayload)
        .when()
        .post("/v1/auth/register")
        .then()
        .statusCode(409);

    String loginPayload =
        "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";

    var loginResponse =
        given(spec)
            .contentType(ContentType.JSON)
            .body(loginPayload)
            .when()
            .post("/v1/auth/login")
            .then()
            .statusCode(200)
            .body("accessToken", notNullValue())
            .body("refreshToken", notNullValue())
            .extract();

    String accessToken = loginResponse.path("accessToken");
    String refreshToken = loginResponse.path("refreshToken");
    assertNotNull(accessToken);

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + accessToken)
        .body("{}")
        .when()
        .post("/v1/auth/validate")
        .then()
        .statusCode(200)
        .body("valid", equalTo(true))
        .body("userId", equalTo(userId.toString()))
        .body("username", equalTo(username));

    var refreshResponse =
        given(spec)
            .contentType(ContentType.JSON)
            .body("{\"refreshToken\":\"" + refreshToken + "\"}")
            .when()
            .post("/v1/auth/refresh")
            .then()
            .statusCode(200)
            .body("accessToken", notNullValue())
            .extract();

    String refreshedAccess = refreshResponse.path("accessToken");

    given(spec)
        .contentType(ContentType.JSON)
        .body("{\"refreshToken\":\"" + refreshToken + "\"}")
        .when()
        .post("/v1/auth/refresh")
        .then()
        .statusCode(401);

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + refreshedAccess)
        .body("{}")
        .when()
        .post("/v1/auth/revoke")
        .then()
        .statusCode(204);

    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + refreshedAccess)
        .body("{}")
        .when()
        .post("/v1/auth/validate")
        .then()
        .statusCode(401);
  }

  @Test
  void invalidLoginReturnsUnauthorized(RequestSpecification spec) {
    int status =
        given(spec)
            .contentType(ContentType.JSON)
            .body("{\"username\":\"nobody\",\"password\":\"wrongpass\"}")
            .when()
            .post("/v1/auth/login")
            .then()
            .extract()
            .statusCode();
    assertEquals(401, status);
  }
}

