package es.nachobrito.sdd.example.adapter.in.rest;

import es.nachobrito.sdd.example.adapter.in.rest.dto.ErrorResponse;
import es.nachobrito.sdd.example.domain.model.AuthException;
import es.nachobrito.sdd.example.domain.model.AuthenticationException;
import es.nachobrito.sdd.example.domain.model.AuthorizationException;
import es.nachobrito.sdd.example.domain.model.DuplicateUserIdentifierException;
import es.nachobrito.sdd.example.domain.model.DuplicateUsernameException;
import es.nachobrito.sdd.example.domain.model.InvalidUserDataException;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

/**
 * Translates authentication and authorization domain exceptions into HTTP error responses.
 */
@Produces
@Singleton
@Requires(classes = {AuthException.class, ExceptionHandler.class})
public class AuthExceptionHandler
    implements ExceptionHandler<AuthException, HttpResponse<ErrorResponse>> {

  @Override
  public HttpResponse<ErrorResponse> handle(HttpRequest request, AuthException exception) {
    if (exception instanceof AuthenticationException) {
      return HttpResponse.status(HttpStatus.UNAUTHORIZED)
          .body(new ErrorResponse(exception.getMessage()));
    }
    if (exception instanceof AuthorizationException) {
      return HttpResponse.status(HttpStatus.FORBIDDEN)
          .body(new ErrorResponse(exception.getMessage()));
    }
    if (exception instanceof DuplicateUsernameException
        || exception instanceof DuplicateUserIdentifierException) {
      return HttpResponse.status(HttpStatus.CONFLICT)
          .body(new ErrorResponse(exception.getMessage()));
    }
    if (exception instanceof InvalidUserDataException) {
      return HttpResponse.badRequest(new ErrorResponse(exception.getMessage()));
    }
    return HttpResponse.badRequest(new ErrorResponse(exception.getMessage()));
  }
}

