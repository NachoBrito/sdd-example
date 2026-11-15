package es.nachobrito.sdd.example.adapter.in.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@MicronautTest
class ProductDiscountIntegrationTestCase {

  @Test
  void resolvesActiveCampaignDiscountsAtRequestTimeAcrossV1AndV2Endpoints(
      RequestSpecification spec) {
    String adminToken = login(spec, "admin", "admin1234");
    assertNotNull(adminToken);
    String catalogToken = login(spec, "catalog", "catalog1234");
    assertNotNull(catalogToken);
    String customerToken = registerAndLoginCustomer(spec);
    assertNotNull(customerToken);

    UUID audioCat = UUID.randomUUID();
    UUID accessoriesCat = UUID.randomUUID();
    createCategory(spec, adminToken, audioCat, "Audio-" + audioCat.toString().substring(0, 8));
    createCategory(
        spec,
        adminToken,
        accessoriesCat,
        "Accessories-" + accessoriesCat.toString().substring(0, 8));

    UUID uncategorizedProd = UUID.randomUUID();
    UUID audioProd = UUID.randomUUID();
    UUID accessoryProd = UUID.randomUUID();

    createV1Product(spec, catalogToken, uncategorizedProd, 1_500);
    createV2Product(spec, catalogToken, audioProd, audioCat, 12_000);
    createV2Product(spec, catalogToken, accessoryProd, accessoriesCat, 900);

    Instant now = Instant.now();
    String pastStart = now.minusSeconds(3_600).toString();
    String futureEnd = now.plusSeconds(3_600).toString();

    UUID audioCampaign = UUID.randomUUID();
    createCampaign(
        spec, adminToken, audioCampaign, "Audio-High-" + audioCampaign.toString().substring(0, 8),
        "ACTIVE", pastStart, futureEnd, 15,
        "[{\"predicateType\":\"IN_CATEGORY\",\"value\":\"" + audioCat + "\"},"
            + "{\"predicateType\":\"IS_MORE_EXPENSIVE_THAN\",\"value\":\"5000\"}]");

    UUID nonAudioCheapCampaign = UUID.randomUUID();
    createCampaign(
        spec, adminToken, nonAudioCheapCampaign,
        "Budget-Other-" + nonAudioCheapCampaign.toString().substring(0, 8),
        "ACTIVE", pastStart, futureEnd, 10,
        "[{\"predicateType\":\"NOT_IN_CATEGORY\",\"value\":\"" + audioCat + "\"},"
            + "{\"predicateType\":\"IS_CHEAPER_THAN\",\"value\":\"2000\"}]");

    UUID storewideCampaign = UUID.randomUUID();
    createCampaign(
        spec, adminToken, storewideCampaign,
        "Storewide-" + storewideCampaign.toString().substring(0, 8),
        "ACTIVE", pastStart, futureEnd, 5, "[]");

    UUID draftCampaign = UUID.randomUUID();
    createCampaign(
        spec, adminToken, draftCampaign, "Draft-" + draftCampaign.toString().substring(0, 8),
        "DRAFT", pastStart, futureEnd, 50, "[]");

    String expiredStart = now.minusSeconds(7_200).toString();
    String expiredEnd = now.minusSeconds(3_600).toString();
    UUID expiredCampaign = UUID.randomUUID();
    createCampaign(
        spec, adminToken, expiredCampaign,
        "Expired-" + expiredCampaign.toString().substring(0, 8),
        "ACTIVE", expiredStart, expiredEnd, 40, "[]");

    given(spec)
        .header("Authorization", "Bearer " + customerToken)
        .when()
        .get("/v1/products/" + uncategorizedProd)
        .then()
        .statusCode(200)
        .body("priceInCents", equalTo(1_500))
        .body("discounts.campaignId",
            hasItems(nonAudioCheapCampaign.toString(), storewideCampaign.toString()))
        .body("discounts.campaignId", not(hasItem(audioCampaign.toString())))
        .body("discounts.campaignId", not(hasItem(draftCampaign.toString())))
        .body("discounts.campaignId", not(hasItem(expiredCampaign.toString())));

    given(spec)
        .header("Authorization", "Bearer " + catalogToken)
        .when()
        .get("/v2/products/" + audioProd)
        .then()
        .statusCode(200)
        .body("priceInCents", equalTo(12_000))
        .body("discounts.campaignId",
            hasItems(audioCampaign.toString(), storewideCampaign.toString()))
        .body("discounts.campaignId", not(hasItem(nonAudioCheapCampaign.toString())))
        .body("discounts.campaignId", not(hasItem(draftCampaign.toString())));

    given(spec)
        .header("Authorization", "Bearer " + customerToken)
        .queryParam("sku", "ACC-" + accessoryProd.toString().substring(0, 8))
        .when()
        .get("/v2/products")
        .then()
        .statusCode(200)
        .body("[0].priceInCents", equalTo(900))
        .body("[0].discounts.campaignId",
            hasItems(nonAudioCheapCampaign.toString(), storewideCampaign.toString()))
        .body("[0].discounts.campaignId", not(hasItem(audioCampaign.toString())));

    given(spec)
        .header("Authorization", "Bearer " + customerToken)
        .queryParam("sku", "V2A-" + audioProd.toString().substring(0, 8))
        .when()
        .get("/v1/products")
        .then()
        .statusCode(200)
        .body("[0].priceInCents", equalTo(12_000))
        .body("[0].discounts", hasSize(org.hamcrest.Matchers.greaterThanOrEqualTo(2)))
        .body("[0].discounts.campaignId", hasItem(audioCampaign.toString()));

    deleteCampaign(spec, adminToken, audioCampaign);
    deleteCampaign(spec, adminToken, nonAudioCheapCampaign);
    deleteCampaign(spec, adminToken, storewideCampaign);
    deleteCampaign(spec, adminToken, draftCampaign);
    deleteCampaign(spec, adminToken, expiredCampaign);
  }

  private static void createCategory(
      RequestSpecification spec, String token, UUID id, String name) {
    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
        .body("{\"id\":\"" + id + "\",\"name\":\"" + name + "\"}")
        .when()
        .post("/v1/categories")
        .then()
        .statusCode(201);
  }

  private static void createV1Product(
      RequestSpecification spec, String token, UUID id, int priceInCents) {
    String sku = "V1U-" + id.toString().substring(0, 8);
    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
        .body("{\"id\":\"" + id + "\",\"sku\":\"" + sku + "\",\"name\":\"Uncategorized Item\","
            + "\"priceInCents\":" + priceInCents + ",\"currency\":\"EUR\",\"status\":\"ACTIVE\"}")
        .when()
        .post("/v1/products")
        .then()
        .statusCode(201);
  }

  private static void createV2Product(
      RequestSpecification spec, String token, UUID id, UUID categoryId, int priceInCents) {
    String prefix = priceInCents > 5_000 ? "V2A-" : "ACC-";
    String sku = prefix + id.toString().substring(0, 8);
    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
        .body("{\"id\":\"" + id + "\",\"sku\":\"" + sku + "\",\"name\":\"Catalog Item\","
            + "\"priceInCents\":" + priceInCents + ",\"currency\":\"EUR\",\"status\":\"ACTIVE\","
            + "\"categoryId\":\"" + categoryId + "\"}")
        .when()
        .post("/v2/products")
        .then()
        .statusCode(201);
  }

  private static void createCampaign(
      RequestSpecification spec,
      String token,
      UUID id,
      String name,
      String status,
      String startDate,
      String endDate,
      int discountPercent,
      String predicatesJson) {
    given(spec)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + token)
        .body("{\"id\":\"" + id + "\",\"name\":\"" + name + "\",\"status\":\"" + status
            + "\",\"startDate\":\"" + startDate + "\",\"endDate\":\"" + endDate
            + "\",\"discountPercent\":" + discountPercent + ",\"predicates\":" + predicatesJson
            + "}")
        .when()
        .post("/v1/campaigns")
        .then()
        .statusCode(201);
  }

  private static void deleteCampaign(RequestSpecification spec, String token, UUID id) {
    given(spec)
        .header("Authorization", "Bearer " + token)
        .when()
        .delete("/v1/campaigns/" + id)
        .then()
        .statusCode(204);
  }

  private static String registerAndLoginCustomer(RequestSpecification spec) {
    UUID userId = UUID.randomUUID();
    String username = "disccust_" + userId.toString().substring(0, 8);
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

