package es.nachobrito.sdd.example.adapter.in.rest;

import es.nachobrito.sdd.example.adapter.in.rest.dto.CampaignPredicateDto;
import es.nachobrito.sdd.example.adapter.in.rest.dto.CampaignResponse;
import es.nachobrito.sdd.example.adapter.in.rest.dto.CreateCampaignRequest;
import es.nachobrito.sdd.example.adapter.in.rest.dto.UpdateCampaignRequest;
import es.nachobrito.sdd.example.application.port.in.CampaignUseCase;
import es.nachobrito.sdd.example.domain.model.CampaignStatus;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.annotation.Status;
import io.micronaut.security.annotation.Secured;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * REST controller for managing promotional discount campaigns.
 */
@Validated
@Controller("/v1/campaigns")
@Tag(name = "Campaigns", description = "Endpoints for managing promotional discount campaigns")
public class CampaignController {

  private final CampaignUseCase campaignUseCase;

  /**
   * Constructs the controller with the campaign use case port.
   *
   * @param campaignUseCase the composite campaign use case port
   */
  public CampaignController(CampaignUseCase campaignUseCase) {
    this.campaignUseCase = campaignUseCase;
  }

  /**
   * Creates a new promotional campaign with a client-generated identifier.
   *
   * @param request the campaign creation request body
   * @return 201 Created with the campaign and Location header
   */
  @Post
  @Secured({"ADMIN"})
  @Operation(
      summary = "Create campaign",
      description = "Creates a promotional campaign with client-generated UUID")
  @ApiResponse(responseCode = "201", description = "Campaign created successfully")
  @ApiResponse(responseCode = "400", description = "Validation error")
  @ApiResponse(responseCode = "403", description = "ADMIN role required")
  @ApiResponse(responseCode = "409", description = "Campaign identifier already exists")
  public HttpResponse<CampaignResponse> create(@Body @Valid CreateCampaignRequest request) {
    CampaignUseCase.CreateCampaignCommand command = new CampaignUseCase.CreateCampaignCommand(
        request.id(),
        request.name(),
        request.description(),
        request.status(),
        request.startDate(),
        request.endDate(),
        request.discountPercent(),
        request.predicates().stream().map(CampaignPredicateDto::toDomain).toList());
    CampaignResponse created =
        CampaignResponse.fromDomain(campaignUseCase.createCampaign(command));
    return HttpResponse.created(created, URI.create("/v1/campaigns/" + created.id()));
  }

  /**
   * Updates an existing promotional campaign and replaces its predicates.
   *
   * @param id the target campaign identifier
   * @param request the campaign update request body
   * @return 200 OK with the updated campaign
   */
  @Put("/{id}")
  @Secured({"ADMIN"})
  @Operation(
      summary = "Update campaign",
      description = "Updates a promotional campaign and replaces its predicates")
  @ApiResponse(responseCode = "200", description = "Campaign updated successfully")
  @ApiResponse(responseCode = "400", description = "Validation error")
  @ApiResponse(responseCode = "403", description = "ADMIN role required")
  @ApiResponse(responseCode = "404", description = "Campaign not found")
  public CampaignResponse update(
      @PathVariable UUID id,
      @Body @Valid UpdateCampaignRequest request) {
    CampaignUseCase.UpdateCampaignCommand command = new CampaignUseCase.UpdateCampaignCommand(
        id,
        request.name(),
        request.description(),
        request.status(),
        request.startDate(),
        request.endDate(),
        request.discountPercent(),
        request.predicates().stream().map(CampaignPredicateDto::toDomain).toList());
    return CampaignResponse.fromDomain(campaignUseCase.updateCampaign(command));
  }

  /**
   * Lists promotional campaigns matching optional attribute and window overlap filters.
   *
   * @param name optional case-insensitive name substring filter
   * @param description optional case-insensitive description substring filter
   * @param status optional lifecycle status filter
   * @param startDate optional lower overlap bound in UTC
   * @param endDate optional upper overlap bound in UTC
   * @return the list of matching campaigns
   */
  @Get
  @Secured({"ADMIN"})
  @Operation(
      summary = "List campaigns",
      description = "Lists promotional campaigns with optional attribute and date filters")
  @ApiResponse(responseCode = "200", description = "Campaigns retrieved successfully")
  @ApiResponse(responseCode = "400", description = "Invalid filter parameters")
  @ApiResponse(responseCode = "403", description = "ADMIN role required")
  public List<CampaignResponse> list(
      @Nullable @QueryValue String name,
      @Nullable @QueryValue String description,
      @Nullable @QueryValue CampaignStatus status,
      @Nullable @QueryValue Instant startDate,
      @Nullable @QueryValue Instant endDate) {
    CampaignUseCase.ListCampaignsQuery query =
        new CampaignUseCase.ListCampaignsQuery(name, description, status, startDate, endDate);
    return campaignUseCase.listCampaigns(query).stream()
        .map(CampaignResponse::fromDomain)
        .toList();
  }

  /**
   * Permanently deletes a promotional campaign by identifier.
   *
   * @param id the campaign identifier
   * @return 204 No Content
   */
  @Delete("/{id}")
  @Status(HttpStatus.NO_CONTENT)
  @Secured({"ADMIN"})
  @Operation(
      summary = "Delete campaign",
      description = "Deletes a promotional campaign by identifier")
  @ApiResponse(responseCode = "204", description = "Campaign deleted successfully")
  @ApiResponse(responseCode = "403", description = "ADMIN role required")
  @ApiResponse(responseCode = "404", description = "Campaign not found")
  public HttpResponse<Void> delete(@PathVariable UUID id) {
    campaignUseCase.deleteCampaign(id);
    return HttpResponse.noContent();
  }
}

