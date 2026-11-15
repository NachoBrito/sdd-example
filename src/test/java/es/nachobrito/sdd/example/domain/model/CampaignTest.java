package es.nachobrito.sdd.example.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CampaignTest {

  private static final UUID ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final Instant START = Instant.parse("2026-11-10T00:00:00Z");
  private static final Instant END = Instant.parse("2026-11-20T00:00:00Z");

  @Test
  void createsValidCampaignTrimsStringsAndProtectsPredicatesList() {
    CampaignPredicate predicate =
        new CampaignPredicate(PredicateType.IS_CHEAPER_THAN, "5000");
    Campaign campaign = new Campaign(
        ID, "  Black Friday  ", "  Holiday deal  ",
        CampaignStatus.ACTIVE, START, END, 25, List.of(predicate));

    assertEquals("Black Friday", campaign.name());
    assertEquals("Holiday deal", campaign.description());
    assertEquals(25, campaign.discountPercent());
    assertThrows(UnsupportedOperationException.class, () -> campaign.predicates().add(predicate));

    Campaign minZero = new Campaign(
        ID, "N".repeat(Campaign.MAX_NAME_LENGTH), null,
        CampaignStatus.DRAFT, START, START, 0, null);
    assertNull(minZero.description());
    assertTrue(minZero.predicates().isEmpty());

    Campaign maxDiscount = new Campaign(
        ID, "Max Deal", "D".repeat(Campaign.MAX_DESCRIPTION_LENGTH),
        CampaignStatus.ACTIVE, START, END, 100, List.of());
    assertEquals(100, maxDiscount.discountPercent());
  }

  @Test
  void evaluatesActivityWindowAndToActiveDiscount() {
    Campaign active = new Campaign(
        ID, "Spring Sale", null, CampaignStatus.ACTIVE, START, END, 15, List.of());
    assertTrue(active.isActiveAt(START));
    assertTrue(active.isActiveAt(START.plusSeconds(60)));
    assertTrue(active.isActiveAt(END));
    assertFalse(active.isActiveAt(START.minusSeconds(1)));
    assertFalse(active.isActiveAt(END.plusSeconds(1)));
    assertThrows(NullPointerException.class, () -> active.isActiveAt(null));

    Campaign draft = new Campaign(
        ID, "Draft", null, CampaignStatus.DRAFT, START, END, 15, List.of());
    assertFalse(draft.isActiveAt(START.plusSeconds(60)));

    ActiveDiscount discount = active.toActiveDiscount();
    assertEquals(ID, discount.campaignId());
    assertEquals("Spring Sale", discount.campaignName());
    assertEquals(15, discount.discountPercent());
  }

  @Test
  void rejectsInvalidFieldsAndNullPredicateElements() {
    assertThrows(
        InvalidCampaignDataException.class,
        () -> new Campaign(null, "Name", null, CampaignStatus.DRAFT, START, END, 10, List.of()));
    assertThrows(
        InvalidCampaignDataException.class,
        () -> new Campaign(ID, null, null, CampaignStatus.DRAFT, START, END, 10, List.of()));
    assertThrows(
        InvalidCampaignDataException.class,
        () -> new Campaign(ID, "  ", null, CampaignStatus.DRAFT, START, END, 10, List.of()));
    assertThrows(
        InvalidCampaignDataException.class,
        () -> new Campaign(
            ID, "N".repeat(Campaign.MAX_NAME_LENGTH + 1), null,
            CampaignStatus.DRAFT, START, END, 10, List.of()));
    assertThrows(
        InvalidCampaignDataException.class,
        () -> new Campaign(
            ID, "Valid", "D".repeat(Campaign.MAX_DESCRIPTION_LENGTH + 1),
            CampaignStatus.DRAFT, START, END, 10, List.of()));
    assertThrows(
        InvalidCampaignDataException.class,
        () -> new Campaign(ID, "Valid", null, null, START, END, 10, List.of()));
    assertThrows(
        InvalidCampaignDataException.class,
        () -> new Campaign(ID, "Valid", null, CampaignStatus.DRAFT, null, END, 10, List.of()));
    assertThrows(
        InvalidCampaignDataException.class,
        () -> new Campaign(ID, "Valid", null, CampaignStatus.DRAFT, START, null, 10, List.of()));
    assertThrows(
        InvalidCampaignDataException.class,
        () -> new Campaign(
            ID, "Valid", null, CampaignStatus.DRAFT, END, START, 10, List.of()));
    assertThrows(
        InvalidCampaignDataException.class,
        () -> new Campaign(ID, "Valid", null, CampaignStatus.DRAFT, START, END, -1, List.of()));
    assertThrows(
        InvalidCampaignDataException.class,
        () -> new Campaign(ID, "Valid", null, CampaignStatus.DRAFT, START, END, 101, List.of()));

    List<CampaignPredicate> withNull = Arrays.asList(
        new CampaignPredicate(PredicateType.IS_CHEAPER_THAN, "100"), null);
    assertThrows(
        InvalidCampaignDataException.class,
        () -> new Campaign(ID, "Valid", null, CampaignStatus.DRAFT, START, END, 10, withNull));
  }
}

