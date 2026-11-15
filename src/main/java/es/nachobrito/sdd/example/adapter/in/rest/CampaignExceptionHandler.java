package es.nachobrito.sdd.example.adapter.in.rest;

import es.nachobrito.sdd.example.adapter.in.rest.dto.ErrorResponse;
import es.nachobrito.sdd.example.domain.model.CampaignException;
import es.nachobrito.sdd.example.domain.model.CampaignNotFoundException;
import es.nachobrito.sdd.example.domain.model.DuplicateCampaignIdentifierException;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

/**
 * Translates campaign domain exceptions into HTTP error responses.
 */
@Produces
@Singleton
@Requires(classes = {CampaignException.class, ExceptionHandler.class})
public class CampaignExceptionHandler
    implements ExceptionHandler<CampaignException, HttpResponse<ErrorResponse>> {

  /**
   * Handles campaign domain exceptions.
   *
   * @param request the HTTP request
   * @param exception the campaign exception
   * @return the HTTP error response
   */
  @Override
  public HttpResponse<ErrorResponse> handle(
      HttpRequest request, CampaignException exception) {
    if (exception instanceof CampaignNotFoundException) {
      return HttpResponse.notFound(new ErrorResponse(exception.getMessage()));
    }
    if (exception instanceof DuplicateCampaignIdentifierException) {
      return HttpResponse.status(HttpStatus.CONFLICT)
          .body(new ErrorResponse(exception.getMessage()));
    }
    return HttpResponse.badRequest(new ErrorResponse(exception.getMessage()));
  }
}

