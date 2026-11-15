package es.nachobrito.sdd.example.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CampaignPredicateTest {

  private static final UUID CAT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID OTHER_CAT_ID =
      UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final Instant NOW = Instant.parse("2026-11-14T12:00:00Z");

  @Test
  void acceptsValidCategoryAndPricePredicatesAndTrimsValues() {
    CampaignPredicate inCat =
        new CampaignPredicate(PredicateType.IN_CATEGORY, "  " + CAT_ID + "  ");
    assertEquals(CAT_ID.toString(), inCat.value());

    CampaignPredicate cheaper =
        new CampaignPredicate(PredicateType.IS_CHEAPER_THAN, " 5000 ");
    assertEquals("5000", cheaper.value());

    CampaignPredicate zeroPrice =
        new CampaignPredicate(PredicateType.IS_MORE_EXPENSIVE_THAN, "0");
    assertEquals("0", zeroPrice.value());

    Product matchProduct = Product.create(
        UUID.randomUUID(), "SKU-1", "Mouse", null, 3_000, "EUR", ProductStatus.ACTIVE, CAT_ID, NOW);
    assertTrue(inCat.matches(matchProduct));
    assertTrue(cheaper.matches(matchProduct));
    assertTrue(zeroPrice.matches(matchProduct));
  }

  @Test
  void rejectsInvalidPredicateTypesAndValues() {
    assertThrows(
        InvalidCampaignDataException.class,
        () -> new CampaignPredicate(null, "100"));
    assertThrows(
        InvalidCampaignDataException.class,
        () -> new CampaignPredicate(PredicateType.IN_CATEGORY, null));
    assertThrows(
        InvalidCampaignDataException.class,
        () -> new CampaignPredicate(PredicateType.IN_CATEGORY, "   "));
    assertThrows(
        InvalidCampaignDataException.class,
        () -> new CampaignPredicate(
            PredicateType.IN_CATEGORY, "A".repeat(CampaignPredicate.MAX_VALUE_LENGTH + 1)));
    InvalidCampaignDataException badUuid = assertThrows(
        InvalidCampaignDataException.class,
        () -> new CampaignPredicate(PredicateType.IN_CATEGORY, "not-a-uuid"));
    assertNotNull(badUuid.getCause());
    assertThrows(
        InvalidCampaignDataException.class,
        () -> new CampaignPredicate(PredicateType.NOT_IN_CATEGORY, "invalid"));
    assertThrows(
        InvalidCampaignDataException.class,
        () -> new CampaignPredicate(PredicateType.IS_CHEAPER_THAN, "-1"));
    InvalidCampaignDataException badNumber = assertThrows(
        InvalidCampaignDataException.class,
        () -> new CampaignPredicate(PredicateType.IS_MORE_EXPENSIVE_THAN, "abc"));
    assertNotNull(badNumber.getCause());

    CampaignPredicate pred =
        new CampaignPredicate(PredicateType.NOT_IN_CATEGORY, OTHER_CAT_ID.toString());
    assertThrows(NullPointerException.class, () -> pred.matches(null));
  }
}

