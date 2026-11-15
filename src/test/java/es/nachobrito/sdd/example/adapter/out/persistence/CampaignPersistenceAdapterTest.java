package es.nachobrito.sdd.example.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.domain.model.Campaign;
import es.nachobrito.sdd.example.domain.model.CampaignPredicate;
import es.nachobrito.sdd.example.domain.model.CampaignStatus;
import es.nachobrito.sdd.example.domain.model.PredicateType;
import io.micronaut.data.model.Sort;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CampaignPersistenceAdapterTest {

  private static final UUID ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final UUID ID_NO_PRED =
      UUID.fromString("55555555-5555-5555-5555-555555555555");
  private static final Instant START = Instant.parse("2026-11-10T00:00:00Z");
  private static final Instant END = Instant.parse("2026-11-20T00:00:00Z");

  @Mock
  private CampaignJdbcRepository campaignRepository;

  @Mock
  private CampaignPredicateJdbcRepository predicateRepository;

  private CampaignPersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new CampaignPersistenceAdapter(campaignRepository, predicateRepository);
  }

  @Test
  void saveAndUpdatePersistCampaignAndPredicates() {
    CampaignPredicate predicate =
        new CampaignPredicate(PredicateType.IS_CHEAPER_THAN, "5000");
    Campaign campaign = new Campaign(
        ID, "Flash", "Fast sale", CampaignStatus.ACTIVE, START, END, 20, List.of(predicate));
    CampaignEntity entity = CampaignEntity.fromDomain(campaign);
    when(campaignRepository.insert(entity)).thenReturn(entity);
    when(campaignRepository.update(entity)).thenReturn(entity);

    assertEquals(campaign, adapter.save(campaign));
    verify(predicateRepository).insert(any(CampaignPredicateEntity.class));

    assertEquals(campaign, adapter.update(campaign));
    verify(predicateRepository).deleteByCampaignId(ID);
    verify(predicateRepository, times(2)).insert(any(CampaignPredicateEntity.class));

    assertThrows(NullPointerException.class, () -> adapter.save(null));
    assertThrows(NullPointerException.class, () -> adapter.update(null));
    assertThrows(NullPointerException.class, () -> CampaignEntity.fromDomain(null));
    assertThrows(
        NullPointerException.class,
        () -> CampaignPredicateEntity.fromDomain(null, ID, predicate));
  }

  @Test
  void findByIdExistsAndDeleteDelegateProperly() {
    CampaignEntity entity = new CampaignEntity(
        ID, "Flash", null, "ACTIVE", START, END, 20);
    CampaignPredicateEntity predicateEntity = new CampaignPredicateEntity(
        UUID.randomUUID(), ID, "IS_CHEAPER_THAN", "5000");
    when(campaignRepository.findById(ID)).thenReturn(Optional.of(entity));
    when(predicateRepository.findByCampaignId(ID)).thenReturn(List.of(predicateEntity));

    Optional<Campaign> found = adapter.findById(ID);
    assertTrue(found.isPresent());
    assertEquals(1, found.get().predicates().size());

    when(campaignRepository.existsById(ID)).thenReturn(true);
    assertTrue(adapter.existsById(ID));
    when(campaignRepository.existsById(ID)).thenReturn(false);
    assertFalse(adapter.existsById(ID));

    adapter.deleteById(ID);
    verify(campaignRepository).deleteById(ID);

    assertThrows(NullPointerException.class, () -> adapter.findById(null));
    assertThrows(NullPointerException.class, () -> adapter.existsById(null));
    assertThrows(NullPointerException.class, () -> adapter.deleteById(null));
  }

  @Test
  void findAllAndFindActiveAtHydratePredicatesAndHandleEmptyResults() {
    Sort sort = Sort.of(Sort.Order.asc("name"));
    when(campaignRepository.search("flash", null, "ACTIVE", START, END, sort))
        .thenReturn(List.of());

    List<Campaign> empty =
        adapter.findAll("  flash ", "   ", CampaignStatus.ACTIVE, START, END);
    assertTrue(empty.isEmpty());
    verifyNoInteractions(predicateRepository);

    CampaignEntity withPred = new CampaignEntity(
        ID, "Alpha", "Desc", "ACTIVE", START, END, 10);
    CampaignEntity noPred = new CampaignEntity(
        ID_NO_PRED, "Beta", null, "ACTIVE", START, END, 5);
    CampaignPredicateEntity predEntity = new CampaignPredicateEntity(
        UUID.randomUUID(), ID, "IS_MORE_EXPENSIVE_THAN", "100");
    when(campaignRepository.findActiveAt(START, sort)).thenReturn(List.of(withPred, noPred));
    when(predicateRepository.findByCampaignIdIn(List.of(ID, ID_NO_PRED)))
        .thenReturn(List.of(predEntity));

    List<Campaign> active = adapter.findActiveAt(START);
    assertEquals(2, active.size());
    assertEquals(1, active.get(0).predicates().size());
    assertTrue(active.get(1).predicates().isEmpty());

    when(campaignRepository.search(null, "Desc", null, null, null, sort))
        .thenReturn(List.of(withPred));
    when(predicateRepository.findByCampaignIdIn(List.of(ID))).thenReturn(List.of(predEntity));

    List<Campaign> filtered = adapter.findAll(null, "Desc", null, null, null);
    assertEquals(1, filtered.size());
    assertEquals(1, filtered.get(0).predicates().size());

    assertThrows(NullPointerException.class, () -> adapter.findActiveAt(null));
  }
}

