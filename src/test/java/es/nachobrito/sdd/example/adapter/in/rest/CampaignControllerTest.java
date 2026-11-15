package es.nachobrito.sdd.example.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.nachobrito.sdd.example.adapter.in.rest.dto.CampaignPredicateDto;
import es.nachobrito.sdd.example.adapter.in.rest.dto.CampaignResponse;
import es.nachobrito.sdd.example.adapter.in.rest.dto.CreateCampaignRequest;
import es.nachobrito.sdd.example.adapter.in.rest.dto.UpdateCampaignRequest;
import es.nachobrito.sdd.example.application.port.in.CampaignUseCase;
import es.nachobrito.sdd.example.application.port.in.CreateCampaignUseCase.CreateCampaignCommand;
import es.nachobrito.sdd.example.application.port.in.ListCampaignsUseCase.ListCampaignsQuery;
import es.nachobrito.sdd.example.application.port.in.UpdateCampaignUseCase.UpdateCampaignCommand;
import es.nachobrito.sdd.example.domain.model.Campaign;
import es.nachobrito.sdd.example.domain.model.CampaignPredicate;
import es.nachobrito.sdd.example.domain.model.CampaignStatus;
import es.nachobrito.sdd.example.domain.model.PredicateType;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CampaignControllerTest {

  private static final UUID ID = UUID.fromString("88888888-8888-8888-8888-888888888888");
  private static final Instant START = Instant.parse("2026-11-10T00:00:00Z");
  private static final Instant END = Instant.parse("2026-11-20T00:00:00Z");

  @Mock
  private CampaignUseCase campaignUseCase;

  private CampaignController controller;

  @BeforeEach
  void setUp() {
    controller = new CampaignController(campaignUseCase);
  }

  @Test
  void createReturnsCreatedWithLocationAndBody() {
    CampaignPredicateDto predicateDto =
        new CampaignPredicateDto(PredicateType.IS_CHEAPER_THAN, "2000");
    CreateCampaignRequest request = new CreateCampaignRequest(
        ID, "Promo", "Desc", CampaignStatus.ACTIVE, START, END, 15, List.of(predicateDto));
    Campaign campaign = new Campaign(
        ID, "Promo", "Desc", CampaignStatus.ACTIVE, START, END, 15,
        List.of(new CampaignPredicate(PredicateType.IS_CHEAPER_THAN, "2000")));
    when(campaignUseCase.createCampaign(any(CreateCampaignCommand.class))).thenReturn(campaign);

    HttpResponse<CampaignResponse> response = controller.create(request);

    assertEquals(HttpStatus.CREATED, response.getStatus());
    assertEquals("/v1/campaigns/" + ID, response.getHeaders().get("Location"));
    CampaignResponse body = Objects.requireNonNull(response.body());
    assertEquals(15, body.discountPercent());
    assertEquals(1, body.predicates().size());
    assertThrows(
        UnsupportedOperationException.class,
        () -> body.predicates().add(predicateDto));
  }

  @Test
  void updateListAndDeleteDelegateToUseCase() {
    UpdateCampaignRequest updateRequest = new UpdateCampaignRequest(
        "Updated", null, CampaignStatus.INACTIVE, START, END, 30, null);
    Campaign updated = new Campaign(
        ID, "Updated", null, CampaignStatus.INACTIVE, START, END, 30, List.of());
    when(campaignUseCase.updateCampaign(any(UpdateCampaignCommand.class))).thenReturn(updated);

    CampaignResponse updatedResponse = controller.update(ID, updateRequest);
    assertEquals("Updated", updatedResponse.name());
    assertTrue(updateRequest.predicates().isEmpty());

    when(campaignUseCase.listCampaigns(
            new ListCampaignsQuery("up", null, CampaignStatus.INACTIVE, START, END)))
        .thenReturn(List.of(updated));
    List<CampaignResponse> listed =
        controller.list("up", null, CampaignStatus.INACTIVE, START, END);
    assertEquals(1, listed.size());

    HttpResponse<Void> deleted = controller.delete(ID);
    assertEquals(HttpStatus.NO_CONTENT, deleted.getStatus());
    verify(campaignUseCase).deleteCampaign(ID);

    CampaignResponse nullPredResponse = new CampaignResponse(
        ID, "Name", null, CampaignStatus.DRAFT, START, END, 5, null);
    assertTrue(nullPredResponse.predicates().isEmpty());
    assertThrows(NullPointerException.class, () -> CampaignResponse.fromDomain(null));
    assertThrows(NullPointerException.class, () -> CampaignPredicateDto.fromDomain(null));
  }
}

