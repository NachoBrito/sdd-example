package es.nachobrito.sdd.example.adapter.in.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

/**
 * Integration tests verifying that artificial error controller routes do not exist.
 */
@MicronautTest
class ErrorRouteRemovalTestCase {

  @Test
  void productErrorsRouteDoesNotExist(RequestSpecification spec) {
    int status = given(spec)
        .when()
        .get("/v1/errors")
        .then()
        .extract()
        .statusCode();
    assertEquals(404, status);
  }

  @Test
  void orderErrorsRouteDoesNotExist(RequestSpecification spec) {
    int status = given(spec)
        .when()
        .get("/v1/order-errors")
        .then()
        .extract()
        .statusCode();
    assertEquals(404, status);
  }
}

