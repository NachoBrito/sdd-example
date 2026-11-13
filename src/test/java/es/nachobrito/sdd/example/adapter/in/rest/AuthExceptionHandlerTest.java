package es.nachobrito.sdd.example.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import es.nachobrito.sdd.example.adapter.in.rest.dto.ErrorResponse;
import es.nachobrito.sdd.example.domain.model.AuthException;
import es.nachobrito.sdd.example.domain.model.AuthenticationException;
import es.nachobrito.sdd.example.domain.model.AuthorizationException;
import es.nachobrito.sdd.example.domain.model.DuplicateUserIdentifierException;
import es.nachobrito.sdd.example.domain.model.DuplicateUsernameException;
import es.nachobrito.sdd.example.domain.model.InvalidUserDataException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthExceptionHandlerTest {

  private final AuthExceptionHandler handler = new AuthExceptionHandler();
  private final HttpRequest<?> request = mock(HttpRequest.class);

  @Test
  void mapsAuthExceptionsToExpectedHttpStatuses() {
    HttpResponse<ErrorResponse> unauth =
        handler.handle(request, new AuthenticationException("bad creds"));
    assertEquals(HttpStatus.UNAUTHORIZED, unauth.getStatus());
    assertEquals("bad creds", unauth.body().message());

    HttpResponse<ErrorResponse> forbidden =
        handler.handle(request, new AuthorizationException("denied"));
    assertEquals(HttpStatus.FORBIDDEN, forbidden.getStatus());

    HttpResponse<ErrorResponse> dupName =
        handler.handle(request, new DuplicateUsernameException("alice"));
    assertEquals(HttpStatus.CONFLICT, dupName.getStatus());

    HttpResponse<ErrorResponse> dupId =
        handler.handle(request, new DuplicateUserIdentifierException(UUID.randomUUID()));
    assertEquals(HttpStatus.CONFLICT, dupId.getStatus());

    HttpResponse<ErrorResponse> invalid =
        handler.handle(request, new InvalidUserDataException("invalid"));
    assertEquals(HttpStatus.BAD_REQUEST, invalid.getStatus());

    AuthException generic = new GenericAuthException("other");
    HttpResponse<ErrorResponse> fallback = handler.handle(request, generic);
    assertEquals(HttpStatus.BAD_REQUEST, fallback.getStatus());
  }

  private static final class GenericAuthException extends AuthException {
    private static final long serialVersionUID = 1L;

    private GenericAuthException(String message) {
      super(message);
    }
  }
}

