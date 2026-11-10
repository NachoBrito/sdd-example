package es.nachobrito.sdd.example.adapter.in.rest.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorResponseTest {

  @Test
  void detailsWithNullDefaultsToEmptyList() {
    ErrorResponse response = new ErrorResponse("message", null);
    assertTrue(response.details().isEmpty());
  }

  @Test
  void singleArgConstructorSetsEmptyDetails() {
    ErrorResponse response = new ErrorResponse("message");
    assertEquals("message", response.message());
  }

  @Test
  void detailsReturnsProvidedList() {
    ErrorResponse response = new ErrorResponse("message", List.of("detail 1"));
    assertEquals(List.of("detail 1"), response.details());
  }
}

