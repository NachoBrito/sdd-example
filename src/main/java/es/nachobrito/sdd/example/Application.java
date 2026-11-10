package es.nachobrito.sdd.example;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

/** Starts the application. */
@OpenAPIDefinition(
    info = @Info(title = "sdd-safe", version = "${api.version}", description = "sdd-safe REST API"))
public final class Application {

  private Application() {}

  /**
   * Starts the Micronaut application context and the HTTP server.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    Micronaut.run(Application.class, args);
  }
}

