package es.nachobrito.sdd.example.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.nachobrito.sdd.example.adapter.in.rest.dto.ErrorResponse;
import es.nachobrito.sdd.example.domain.model.CampaignNotFoundException;
import es.nachobrito.sdd.example.domain.model.DuplicateCampaignIdentifierException;
import es.nachobrito.sdd.example.domain.model.InvalidCampaignDataException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CampaignExceptionHandlerTest {

  private static final UUID ID = UUID.fromString("77777777-7777-7777-7777-777777777777");

  private final CampaignExceptionHandler handler = new CampaignExceptionHandler();

  @Test
  void mapsCampaignExceptionsToExpectedHttpStatuses() {
    HttpRequest<?> request = HttpRequest.GET("/v1/campaigns");

    HttpResponse<ErrorResponse> notFound =
        handler.handle(request, new CampaignNotFoundException(ID));
    assertEquals(HttpStatus.NOT_FOUND, notFound.getStatus());

    HttpResponse<ErrorResponse> duplicate =
        handler.handle(request, new DuplicateCampaignIdentifierException(ID));
    assertEquals(HttpStatus.CONFLICT, duplicate.getStatus());

    HttpResponse<ErrorResponse> invalid =
        handler.handle(request, new InvalidCampaignDataException("Invalid campaign"));
    assertEquals(HttpStatus.BAD_REQUEST, invalid.getStatus());
    assertEquals("Invalid campaign", invalid.body().message());
  }
}

