package es.nachobrito.sdd.example.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.application.port.in.CreateCampaignUseCase.CreateCampaignCommand;
import es.nachobrito.sdd.example.application.port.in.ListCampaignsUseCase.ListCampaignsQuery;
import es.nachobrito.sdd.example.application.port.in.UpdateCampaignUseCase.UpdateCampaignCommand;
import es.nachobrito.sdd.example.application.port.out.CampaignRepositoryPort;
import es.nachobrito.sdd.example.domain.model.Campaign;
import es.nachobrito.sdd.example.domain.model.CampaignNotFoundException;
import es.nachobrito.sdd.example.domain.model.CampaignPredicate;
import es.nachobrito.sdd.example.domain.model.CampaignStatus;
import es.nachobrito.sdd.example.domain.model.DuplicateCampaignIdentifierException;
import es.nachobrito.sdd.example.domain.model.InvalidCampaignDataException;
import es.nachobrito.sdd.example.domain.model.PredicateType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

  private static final UUID ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
  private static final Instant START = Instant.parse("2026-11-10T00:00:00Z");
  private static final Instant END = Instant.parse("2026-11-20T00:00:00Z");

  @Mock
  private CampaignRepositoryPort repository;

  private CampaignService service;

  @BeforeEach
  void setUp() {
    Clock clock = Clock.fixed(Instant.parse("2026-11-15T00:00:00Z"), ZoneOffset.UTC);
    service = new CampaignService(repository, clock);
  }

  @Test
  void createCampaignValidatesPersistsAndRejectsDuplicates() {
    List<CampaignPredicate> predicates =
        List.of(new CampaignPredicate(PredicateType.IS_CHEAPER_THAN, "5000"));
    CreateCampaignCommand command = new CreateCampaignCommand(
        ID, "Promo", "Desc", CampaignStatus.ACTIVE, START, END, 15, predicates);
    Campaign expected = new Campaign(
        ID, "Promo", "Desc", CampaignStatus.ACTIVE, START, END, 15, predicates);

    when(repository.existsById(ID)).thenReturn(false);
    when(repository.save(expected)).thenReturn(expected);
    assertEquals(expected, service.createCampaign(command));

    when(repository.existsById(ID)).thenReturn(true);
    assertThrows(
        DuplicateCampaignIdentifierException.class,
        () -> service.createCampaign(command));

    CreateCampaignCommand invalid = new CreateCampaignCommand(
        ID, "Bad", null, CampaignStatus.DRAFT, END, START, 10, List.of());
    assertThrows(InvalidCampaignDataException.class, () -> service.createCampaign(invalid));
    assertThrows(NullPointerException.class, () -> service.createCampaign(null));

    CreateCampaignCommand nullPredicates = new CreateCampaignCommand(
        ID, "Open", null, CampaignStatus.DRAFT, START, END, 10, null);
    assertTrue(nullPredicates.predicates().isEmpty());
    assertThrows(
        UnsupportedOperationException.class,
        () -> command.predicates().add(predicates.get(0)));
  }

  @Test
  void updateCampaignValidatesUpdatesAndRejectsMissing() {
    UpdateCampaignCommand command = new UpdateCampaignCommand(
        ID, "Updated", null, CampaignStatus.INACTIVE, START, END, 20, List.of());
    when(repository.existsById(ID)).thenReturn(false);
    assertThrows(CampaignNotFoundException.class, () -> service.updateCampaign(command));
    verify(repository, never()).update(any());

    Campaign updated = new Campaign(
        ID, "Updated", null, CampaignStatus.INACTIVE, START, END, 20, List.of());
    when(repository.existsById(ID)).thenReturn(true);
    when(repository.update(updated)).thenReturn(updated);
    assertEquals(updated, service.updateCampaign(command));

    assertThrows(NullPointerException.class, () -> service.updateCampaign(null));
    UpdateCampaignCommand nullIdCommand = new UpdateCampaignCommand(
        null, "Updated", null, CampaignStatus.INACTIVE, START, END, 20, null);
    assertTrue(nullIdCommand.predicates().isEmpty());
    assertThrows(
        UnsupportedOperationException.class,
        () -> command.predicates().add(
            new CampaignPredicate(PredicateType.IS_CHEAPER_THAN, "100")));
    assertThrows(NullPointerException.class, () -> service.updateCampaign(nullIdCommand));
  }

  @Test
  void listCampaignsValidatesWindowAndDelegatesFilters() {
    Campaign expected = new Campaign(
        ID, "Promo", null, CampaignStatus.ACTIVE, START, END, 10, List.of());
    when(repository.findAll("promo", null, CampaignStatus.ACTIVE, START, END))
        .thenReturn(List.of(expected));

    List<Campaign> result = service.listCampaigns(
        new ListCampaignsQuery("promo", null, CampaignStatus.ACTIVE, START, END));
    assertEquals(1, result.size());

    when(repository.findAll(null, null, null, START, null)).thenReturn(List.of(expected));
    assertEquals(
        1,
        service.listCampaigns(new ListCampaignsQuery(null, null, null, START, null)).size());

    when(repository.findAll(null, null, null, null, END)).thenReturn(List.of(expected));
    assertEquals(
        1,
        service.listCampaigns(new ListCampaignsQuery(null, null, null, null, END)).size());

    assertThrows(
        InvalidCampaignDataException.class,
        () -> service.listCampaigns(new ListCampaignsQuery(null, null, null, END, START)));
    assertThrows(NullPointerException.class, () -> service.listCampaigns(null));
  }

  @Test
  void deleteCampaignDeletesExistingOrThrowsWhenMissing() {
    when(repository.existsById(ID)).thenReturn(false);
    assertThrows(CampaignNotFoundException.class, () -> service.deleteCampaign(ID));

    when(repository.existsById(ID)).thenReturn(true);
    service.deleteCampaign(ID);
    verify(repository).deleteById(ID);

    assertThrows(NullPointerException.class, () -> service.deleteCampaign(null));
  }
}

