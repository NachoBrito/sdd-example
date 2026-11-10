package es.nachobrito.sdd.example.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.GeneralCodingRules;
import io.micronaut.data.annotation.Repository;
import io.micronaut.http.annotation.Controller;
import io.micronaut.rabbitmq.annotation.RabbitClient;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import java.util.regex.Pattern;

/**
 * Architecture rules for the hexagonal architecture. Read docs/static-analysis.md for the full
 * description of each rule.
 */
@AnalyzeClasses(
    packages = "es.nachobrito.sdd.example",
    importOptions = {
      ImportOption.DoNotIncludeTests.class,
      HexagonalArchitectureTest.DoNotIncludeGeneratedClasses.class
    })
class HexagonalArchitectureTest {

  private static final String DOMAIN = "..domain..";
  private static final String APPLICATION = "..application..";
  private static final String ADAPTER = "..adapter..";
  private static final String ADAPTER_IN = "..adapter.in..";
  private static final String ADAPTER_OUT = "..adapter.out..";

  private static final Pattern VERSIONED_PATH = Pattern.compile("^/v[1-9]\\d*(/.*)?$");

  // ---------------------------------------------------------------------------------------------
  // Layers
  // ---------------------------------------------------------------------------------------------

  @ArchTest
  static final ArchRule layerDependencies =
      layeredArchitecture()
          .consideringOnlyDependenciesInLayers()
          .withOptionalLayers(true)
          .layer("Domain")
          .definedBy(DOMAIN)
          .layer("Application")
          .definedBy(APPLICATION)
          .layer("Adapter")
          .definedBy(ADAPTER)
          .whereLayer("Adapter")
          .mayNotBeAccessedByAnyLayer()
          .whereLayer("Application")
          .mayOnlyBeAccessedByLayers("Adapter")
          .whereLayer("Domain")
          .mayOnlyBeAccessedByLayers("Application", "Adapter");

  @ArchTest
  static final ArchRule domainIsFrameworkFree =
      classes()
          .that()
          .resideInAPackage(DOMAIN)
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage(DOMAIN, "java..", "org.jspecify..")
          .because("the domain model must not depend on a framework or on infrastructure");

  @ArchTest
  static final ArchRule applicationDoesNotUseInfrastructure =
      noClasses()
          .that()
          .resideInAPackage(APPLICATION)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "io.micronaut.http..",
              "io.micronaut.data..",
              "io.micronaut.rabbitmq..",
              "io.micronaut.serde..",
              "java.sql..",
              "javax.sql..")
          .because("the application layer must access infrastructure only through ports")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule inboundAdaptersDoNotUseOutboundAdapters =
      noClasses()
          .that()
          .resideInAPackage(ADAPTER_IN)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(ADAPTER_OUT)
          .because("inbound adapters must call the application through its inbound ports")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule outboundAdaptersDoNotUseInboundAdapters =
      noClasses()
          .that()
          .resideInAPackage(ADAPTER_OUT)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(ADAPTER_IN)
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule noPackageCycles =
      slices().matching("es.nachobrito.sdd.example.(**)").should().beFreeOfCycles();

  // ---------------------------------------------------------------------------------------------
  // Adapter placement and REST conventions
  // ---------------------------------------------------------------------------------------------

  @ArchTest
  static final ArchRule controllersAreInboundRestAdapters =
      classes()
          .that()
          .areAnnotatedWith(Controller.class)
          .should()
          .resideInAPackage("..adapter.in.rest..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule controllerPathsAreVersioned =
      classes()
          .that()
          .areAnnotatedWith(Controller.class)
          .should(haveVersionedPath())
          .because("all endpoints use versioned paths, for example /v1/orders")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule repositoriesArePersistenceAdapters =
      classes()
          .that()
          .areMetaAnnotatedWith(Repository.class)
          .should()
          .resideInAPackage("..adapter.out.persistence..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule rabbitListenersAreInboundMessagingAdapters =
      classes()
          .that()
          .areAnnotatedWith(RabbitListener.class)
          .should()
          .resideInAPackage("..adapter.in.messaging..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule rabbitClientsAreOutboundMessagingAdapters =
      classes()
          .that()
          .areAnnotatedWith(RabbitClient.class)
          .should()
          .resideInAPackage("..adapter.out.messaging..")
          .allowEmptyShould(true);

  // ---------------------------------------------------------------------------------------------
  // Micronaut-native libraries and general coding rules
  // ---------------------------------------------------------------------------------------------

  @ArchTest
  static final ArchRule useMicronautNativeLibraries =
      noClasses()
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "jakarta.persistence..",
              "javax.persistence..",
              "org.hibernate..",
              "lombok..",
              "com.fasterxml.jackson.databind..",
              "tools.jackson.databind..")
          .because(
              "the project uses Micronaut Data, Micronaut Validation, Micronaut Serialization"
                  + " and Micronaut Sourcegen instead of reflection-based libraries");

  @ArchTest
  static final ArchRule noStandardStreams =
      GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

  @ArchTest
  static final ArchRule noGenericExceptions =
      GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

  @ArchTest
  static final ArchRule noJavaUtilLogging =
      GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

  @ArchTest
  static final ArchRule noFieldInjection = GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

  @ArchTest
  static final ArchRule noJodaTime = GeneralCodingRules.NO_CLASSES_SHOULD_USE_JODATIME;

  private static ArchCondition<JavaClass> haveVersionedPath() {
    return new ArchCondition<>("have a @Controller path that starts with /v{n}") {
      @Override
      public void check(JavaClass item, ConditionEvents events) {
        String path = item.getAnnotationOfType(Controller.class).value();
        boolean satisfied = VERSIONED_PATH.matcher(path).matches();
        String message = item.getName() + " has the @Controller path '" + path + "'";
        events.add(new SimpleConditionEvent(item, satisfied, message));
      }
    };
  }

  /** Ignores the classes that Micronaut annotation processors generate, for example $Definition. */
  static final class DoNotIncludeGeneratedClasses implements ImportOption {
    private static final Pattern GENERATED = Pattern.compile(".*/\\$[^/]*\\.class$");

    @Override
    public boolean includes(com.tngtech.archunit.core.importer.Location location) {
      return !location.matches(GENERATED);
    }
  }
}

