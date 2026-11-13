package es.nachobrito.sdd.example.adapter.in.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

/**
 * Integration tests verifying that artificial error controller routes do not exist.
 */
@MicronautTest
class ErrorRouteRemovalTestCase {

  @Test
  void productErrorsRouteDoesNotExist(RequestSpecification spec) {
    String token = loginAsAdmin(spec);
    int status = given(spec)
        .header("Authorization", "Bearer " + token)
        .when()
        .get("/v1/errors")
        .then()
        .extract()
        .statusCode();
    assertEquals(404, status);
  }

  @Test
  void orderErrorsRouteDoesNotExist(RequestSpecification spec) {
    String token = loginAsAdmin(spec);
    int status = given(spec)
        .header("Authorization", "Bearer " + token)
        .when()
        .get("/v1/order-errors")
        .then()
        .extract()
        .statusCode();
    assertEquals(404, status);
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
}

