package es.nachobrito.sdd.example.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.application.port.out.CampaignRepositoryPort;
import es.nachobrito.sdd.example.domain.model.ActiveDiscount;
import es.nachobrito.sdd.example.domain.model.Campaign;
import es.nachobrito.sdd.example.domain.model.CampaignPredicate;
import es.nachobrito.sdd.example.domain.model.CampaignStatus;
import es.nachobrito.sdd.example.domain.model.PredicateType;
import es.nachobrito.sdd.example.domain.model.Product;
import es.nachobrito.sdd.example.domain.model.ProductStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CampaignDiscountResolutionTest {

  private static final UUID CAT_A = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  private static final Instant NOW = Instant.parse("2026-11-14T12:00:00Z");

  @Mock
  private CampaignRepositoryPort campaignRepository;

  private CampaignService service;

  @BeforeEach
  void setUp() {
    Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    service = new CampaignService(campaignRepository, clock);
  }

  @Test
  void resolvesMultipleMatchingActiveDiscountsAndFiltersInactiveOrOutOfWindow() {
    Campaign matchCat = new Campaign(
        UUID.fromString("11111111-1111-1111-1111-111111111111"),
        "Cat Promo",
        null,
        CampaignStatus.ACTIVE,
        NOW.minusSeconds(60),
        NOW.plusSeconds(60),
        15,
        List.of(new CampaignPredicate(PredicateType.IN_CATEGORY, CAT_A.toString())));
    Campaign storewideBoundary = new Campaign(
        UUID.fromString("22222222-2222-2222-2222-222222222222"),
        "Storewide",
        null,
        CampaignStatus.ACTIVE,
        NOW,
        NOW,
        5,
        List.of());
    Campaign draft = new Campaign(
        UUID.fromString("33333333-3333-3333-3333-333333333333"),
        "Draft Deal",
        null,
        CampaignStatus.DRAFT,
        NOW.minusSeconds(60),
        NOW.plusSeconds(60),
        40,
        List.of());
    Campaign expired = new Campaign(
        UUID.fromString("44444444-4444-4444-4444-444444444444"),
        "Expired",
        null,
        CampaignStatus.ACTIVE,
        NOW.minusSeconds(120),
        NOW.minusSeconds(1),
        30,
        List.of());
    Campaign future = new Campaign(
        UUID.fromString("55555555-5555-5555-5555-555555555555"),
        "Future",
        null,
        CampaignStatus.ACTIVE,
        NOW.plusSeconds(1),
        NOW.plusSeconds(120),
        25,
        List.of());

    when(campaignRepository.findActiveAt(NOW))
        .thenReturn(List.of(matchCat, storewideBoundary, draft, expired, future));

    Product prodMatch = Product.create(
        UUID.randomUUID(), "SKU-M", "Matched", null, 3_000, "EUR",
        ProductStatus.ACTIVE, CAT_A, NOW);
    List<ActiveDiscount> singleDiscounts = service.resolveActiveDiscounts(prodMatch);
    assertEquals(2, singleDiscounts.size());
    assertEquals(15, singleDiscounts.get(0).discountPercent());
    assertEquals(5, singleDiscounts.get(1).discountPercent());

    Product prodOther = Product.create(
        UUID.randomUUID(), "SKU-O", "Other", null, 3_000, "EUR",
        ProductStatus.ACTIVE, null, NOW);
    Map<UUID, List<ActiveDiscount>> batch =
        service.resolveActiveDiscounts(List.of(prodMatch, prodOther));
    assertEquals(2, batch.get(prodMatch.id()).size());
    assertEquals(1, batch.get(prodOther.id()).size());
    assertEquals("Storewide", batch.get(prodOther.id()).getFirst().campaignName());
  }

  @Test
  void handlesEmptyProductsNullInputsAndDefaultClockConstructor() {
    assertTrue(service.resolveActiveDiscounts(List.of()).isEmpty());
    verifyNoInteractions(campaignRepository);

    assertThrows(NullPointerException.class, () -> service.resolveActiveDiscounts((Product) null));
    assertThrows(
        NullPointerException.class,
        () -> service.resolveActiveDiscounts((List<Product>) null));

    CampaignService defaultClockService = new CampaignService(campaignRepository);
    Product product = Product.create(
        UUID.randomUUID(), "SKU-D", "Default Clock", null, 1_000, "EUR",
        ProductStatus.ACTIVE, null, NOW);
    assertTrue(defaultClockService.resolveActiveDiscounts(product).isEmpty());
    verify(campaignRepository).findActiveAt(org.mockito.ArgumentMatchers.any(Instant.class));
  }
}

