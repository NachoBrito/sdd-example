package es.nachobrito.sdd.example.domain.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CampaignMatchingTest {

  private static final UUID CAMPAIGN_ID =
      UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID CAT_A =
      UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  private static final UUID CAT_B =
      UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
  private static final Instant START = Instant.parse("2026-11-01T00:00:00Z");
  private static final Instant END = Instant.parse("2026-11-30T23:59:59Z");

  @Test
  void evaluatesCategoryPredicatesIncludingUncategorizedProducts() {
    Product prodA = productWith(CAT_A, 1_000);
    Product prodB = productWith(CAT_B, 1_000);
    Product uncategorized = productWith(null, 1_000);

    CampaignPredicate inCatA = new CampaignPredicate(PredicateType.IN_CATEGORY, CAT_A.toString());
    assertTrue(inCatA.matches(prodA));
    assertFalse(inCatA.matches(prodB));
    assertFalse(inCatA.matches(uncategorized));

    CampaignPredicate notInCatA =
        new CampaignPredicate(PredicateType.NOT_IN_CATEGORY, CAT_A.toString());
    assertFalse(notInCatA.matches(prodA));
    assertTrue(notInCatA.matches(prodB));
    assertTrue(notInCatA.matches(uncategorized));
  }

  @Test
  void evaluatesStrictPriceThresholdPredicates() {
    Product below = productWith(CAT_A, 4_999);
    Product exact = productWith(CAT_A, 5_000);
    Product above = productWith(CAT_A, 5_001);

    CampaignPredicate cheaper = new CampaignPredicate(PredicateType.IS_CHEAPER_THAN, "5000");
    assertTrue(cheaper.matches(below));
    assertFalse(cheaper.matches(exact));
    assertFalse(cheaper.matches(above));

    CampaignPredicate moreExpensive =
        new CampaignPredicate(PredicateType.IS_MORE_EXPENSIVE_THAN, "5000");
    assertFalse(moreExpensive.matches(below));
    assertFalse(moreExpensive.matches(exact));
    assertTrue(moreExpensive.matches(above));
  }

  @Test
  void campaignMatchesAllPredicatesWithLogicalAndAndEmptyMatchesAll() {
    Campaign openCampaign = new Campaign(
        CAMPAIGN_ID, "Storewide", null, CampaignStatus.ACTIVE, START, END, 10, List.of());
    assertTrue(openCampaign.matches(productWith(null, 0)));
    assertTrue(openCampaign.matches(productWith(CAT_B, 99_999)));

    Campaign targeted = new Campaign(
        CAMPAIGN_ID,
        "Targeted",
        null,
        CampaignStatus.ACTIVE,
        START,
        END,
        20,
        List.of(
            new CampaignPredicate(PredicateType.IN_CATEGORY, CAT_A.toString()),
            new CampaignPredicate(PredicateType.IS_MORE_EXPENSIVE_THAN, "2000"),
            new CampaignPredicate(PredicateType.IS_CHEAPER_THAN, "10000")));

    assertTrue(targeted.matches(productWith(CAT_A, 5_000)));
    assertFalse(targeted.matches(productWith(CAT_A, 2_000)));
    assertFalse(targeted.matches(productWith(CAT_A, 10_000)));
    assertFalse(targeted.matches(productWith(CAT_B, 5_000)));
    assertThrows(NullPointerException.class, () -> targeted.matches(null));
  }

  private static Product productWith(UUID categoryId, int priceInCents) {
    return Product.create(
        UUID.randomUUID(),
        "SKU-TEST",
        "Test Item",
        null,
        priceInCents,
        "EUR",
        ProductStatus.ACTIVE,
        categoryId,
        START);
  }
}

