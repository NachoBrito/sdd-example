package es.nachobrito.sdd.example.adapter.in.rest;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import org.junit.jupiter.api.Test;

class ExceptionHandlerArchitectureTest {

  @Test
  void exceptionHandlersResideInRestAdapterPackage() {
    classes()
        .that()
        .implement(ExceptionHandler.class)
        .should()
        .resideInAPackage("..adapter.in.rest..")
        .andShould()
        .notBeAnnotatedWith(Controller.class)
        .because("exception handlers must be inbound REST adapters and not controllers")
        .check(
            new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("es.nachobrito.sdd.example"));
  }
}
